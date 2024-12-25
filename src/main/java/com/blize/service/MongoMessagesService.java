package com.blize.service;

import com.blize.document.Message;
import com.blize.document.result.MessageGroupItem;
import com.blize.entity.User;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.data.mongodb.core.query.Query;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@Service
public class MongoMessagesService {

    private static final int GROUP_LIMIT = 24;
    private static final int USER_ITEM_LIMIT = 10;
    private static final int USER_ITEM_VIEWS_LIMIT = 24;
    private static final long USER_ITEM_VISIBLE_SECOND = 24 * 60 * 60;

    @Autowired
    private MongoTemplate mongoTemplate;


    public List<MessageGroupItem> groupList(User user, int offset) {

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(new Criteria().orOperator(
                        Criteria.where("from").is(user.getId()),
                        Criteria.where("to").is(user.getId())
                        )),
                Aggregation.sort(Sort.Direction.DESC, "date"),
                context -> new Document(new HashMap<>(){{
                    put("$group", new HashMap<String, Object>() {{
                        put("_id", new HashMap<String, Object>() {{
                            put("$cond", new HashMap<String, Object>() {{
                                put("if", new HashMap<String, Object>() {{
                                    put("$eq", Arrays.asList("$from", user.getId()));
                                }});
                                put("then", "$to");
                                put("else", "$from");
                            }});
                        }});
                        put("unReadCount", new HashMap<String, Object>() {{
                            put("$sum", new HashMap<String, Object>() {{
                                put("$cond", new HashMap<String, Object>() {{
                                    put("if", new HashMap<String, Object>() {{
                                        put("$and", Arrays.asList(
                                                new HashMap<String, Object>() {{
                                                    put("$ne", Arrays.asList("$read", true));
                                                }},
                                                new HashMap<String, Object>() {{
                                                    put("$ne", Arrays.asList("$from", user.getId()));
                                                }}
                                        ));
                                    }});
                                    put("then", 1);
                                    put("else", 0);
                                }});
                            }});
                        }});
                        put("to", new HashMap<String, Object>() {{
                            put("$first","$to");
                        }});
                        put("from", new HashMap<String, Object>() {{
                            put("$first","$from");
                        }});
                        put("text", new HashMap<String, Object>() {{
                            put("$first","$text");
                        }});
                        put("date", new HashMap<String, Object>() {{
                            put("$first","$date");
                        }});
                        put("read", new HashMap<String, Object>() {{
                            put("$first","$read");
                        }});
                    }});
                }}),
                Aggregation.sort(Sort.Direction.DESC, "date"),
                Aggregation.skip(offset),
                Aggregation.limit(GROUP_LIMIT+offset)
        );

        return mongoTemplate.aggregate(aggregation, "message", MessageGroupItem.class).getMappedResults();

    }

    public List<Message> detail(User user, User otherUser, int offset) {
        Query query = new Query();
        Criteria orCriteria = new Criteria();
        orCriteria.orOperator(
                new Criteria().andOperator(
                        Criteria.where("from").is(user.getId()),
                        Criteria.where("to").is(otherUser.getId())
                ),
                new Criteria().andOperator(
                        Criteria.where("to").is(user.getId()),
                        Criteria.where("from").is(otherUser.getId())
                )
        );

        query.addCriteria(orCriteria);
        query.with(Sort.by(Sort.Direction.DESC, "date"));
        query.limit(GROUP_LIMIT);
        query.skip(offset);
        return mongoTemplate.find(query, Message.class);
    }

    public Message add(User user, User otherUser, String text) {

        Message message = new Message();
        message.setFrom(user.getId());
        message.setTo(otherUser.getId());
        message.setText(text);
        message.setRead(false);
        message.setDate(OffsetDateTime.now());
        return mongoTemplate.insert(message);
    }

    public UpdateResult read(User user, User otherUser) {

        Query query = new Query();
        query.addCriteria(Criteria.where("from").is(otherUser.getId()));
        query.addCriteria(Criteria.where("to").is(user.getId()));
        query.addCriteria(Criteria.where("read").ne(true));

        Update update = new Update();
        update.set("read", true);

        return mongoTemplate.updateMulti(query, update, Message.class);
    }

}
