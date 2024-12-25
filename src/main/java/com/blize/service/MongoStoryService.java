package com.blize.service;

import com.blize.document.Story;
import com.blize.document.result.StoryGroup;
import com.blize.document.result.StoryMeItem;
import com.blize.document.result.StoryViewItem;
import com.blize.entity.User;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.MongoExpression;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@Service
public class MongoStoryService {

    public static final int GROUP_LIMIT = 24;
    public static final int USER_ITEM_LIMIT = 10;
    public static final int USER_ITEM_VIEWS_LIMIT = 24;
    public static final long USER_ITEM_VISIBLE_SECOND = 24 * 60 * 60;

    @Autowired
    private MongoTemplate mongoTemplate;

    public Story add(User user, String rootPath, String path, String filename) {
        Story story = new Story();
        story.setFrom(user.getId());
        story.setDate(OffsetDateTime.now());
        story.setRootPath(rootPath);
        story.setPath(path);
        story.setFileName(filename);

        return mongoTemplate.insert(story);
    }

    public List<StoryGroup> groupList(User user, int offset) {

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("from").ne(user.getId())
                        .and("date").gte(OffsetDateTime.now().minusSeconds(USER_ITEM_VISIBLE_SECOND*100))
                        .and("deletedAt").exists(false)),
                context -> new Document(new HashMap<>(){{
                    put("$group", new HashMap<String, Object>() {{
                        put("_id", new HashMap<String, Object>() {{
                            put("mid", "$_id");
                            put("from", "$from");
                            put("date", "$date");
                            put("fileName", "$fileName");
                            put("path", "$path");
                            put("rootPath", "$rootPath");
                            put("seen", new HashMap<String, Object>() {{
                                put("$cond", new HashMap<String, Object>() {{
                                    put("if", new HashMap<String, Object>() {{
                                        put("$and", Arrays.asList(
                                                new HashMap<String, Object>() {{
                                                    put("$isArray", "$views.from");
                                                }},
                                                new HashMap<String, Object>() {{
                                                    put("$in", Arrays.asList(1, "$views.from"));
                                                }}
                                        ));
                                    }});
                                    put("then", true);
                                    put("else", false);
                                }});
                            }});
                        }});
                    }});
                }}),
                Aggregation.sort(Sort.Direction.DESC, "date"),
                context -> new Document(new HashMap<>() {{
                    put("$group", new HashMap<String, Object>() {{
                        put("_id", new HashMap<String, Object>() {{
                            put("_id", "$_id.from");
                        }});
                        put("from", new HashMap<String, Object>() {{
                            put("$first", "$_id.from");
                        }});
                        put("items", new HashMap<String, Object>() {{
                            put("$addToSet", new HashMap<String, Object>() {{
                                put("_id", "$_id.mid");
                                put("from", "$_id.from");
                                put("date", "$_id.date");
                                put("fileName", "$_id.fileName");
                                put("path", "$_id.path");
                                put("rootPath", "$_id.rootPath");
                                put("seen", "$_id.seen");
                            }});
                        }});
                    }});
                }}),
                Aggregation.sort(Sort.Direction.DESC, "items.date"),
                Aggregation.skip(offset),
                Aggregation.limit(GROUP_LIMIT + offset)
        );

        return mongoTemplate
                .aggregate(aggregation, "story", StoryGroup.class)
                .getMappedResults();

    }

    public List<StoryMeItem> meList(User user) {
        Query query = new Query(
                new Criteria().andOperator(
                        Criteria.where("from").is(user.getId()),
                        Criteria.where("deletedAt").exists(false),
                        Criteria.where("date").gte(OffsetDateTime.now().minusSeconds(USER_ITEM_VISIBLE_SECOND*100))

                ))
                .with(Sort.by(Sort.Direction.DESC, "date"))
                .limit(USER_ITEM_LIMIT);

        query.fields()
                .include("from")
                .include("date")
                .include("fileName")
                .include("path")
                .include("rootPath")
                .projectAs(MongoExpression.create("""
                        $cond: [
                                { $isArray: "$views" },
                                { $slice: ["$views", 0, 3] },
                                []
                            ]
                        """), "views")
                .projectAs(MongoExpression.create("""
                        $cond: [
                            { $isArray: "$views" },
                            { $size: "$views" },
                            0
                        ]
                        """), "viewsLength");



        return mongoTemplate.find(query, StoryMeItem.class, "story");
    }

    public List<StoryMeItem> viewList(User user, String _id, Integer offset) {
        Query query = new Query(
            new Criteria().andOperator(
                    Criteria.where("_id").is(_id),
                    Criteria.where("from").is(user.getId()),
                    Criteria.where("deletedAt").exists(false)

            ));

        query.fields()
                .projectAs(MongoExpression.create("""
                        $cond: [
                                { $isArray: "$views" },
                                { $slice: ["$views", %d, %d] },
                                []
                            ]
                        """.formatted(offset, USER_ITEM_VIEWS_LIMIT)
                ), "views");
        query.limit(1);

        return mongoTemplate.find(query, StoryMeItem.class, "story");
    }

    public Object seen(User user, String _id) {

        StoryViewItem viewItem = new StoryViewItem();
        viewItem.setFrom(user.getId());
        viewItem.setDate(OffsetDateTime.now());

        Query query = new Query(
            new Criteria().andOperator(
                    Criteria.where("_id").is(_id),
                    Criteria.where("views.from").ne(user.getId())

            ));

        Update update = new Update();
        update.push("views").atPosition(0).each(viewItem);


        return mongoTemplate.updateFirst(query, update, "story");
    }

    public Object delete(User user, String _id) {

        Query query = new Query(
            new Criteria().andOperator(
                    Criteria.where("_id").is(_id),
                    Criteria.where("from").is(user.getId()),
                    Criteria.where("deletedAt").exists(false)

            ));

        Update update = new Update();
        update.set("deletedAt", true);

        return mongoTemplate.updateFirst(query, update, "story");
    }

    public long activeStoryCount(User user) {
        Query query = new Query(
                new Criteria().andOperator(
                        Criteria.where("from").is(user.getId()),
                        Criteria.where("deletedAt").exists(false),
                        Criteria.where("date").gte(OffsetDateTime.now().minusSeconds(USER_ITEM_VISIBLE_SECOND*100))

                ));

        return mongoTemplate.count(query, "story");
    }

}
