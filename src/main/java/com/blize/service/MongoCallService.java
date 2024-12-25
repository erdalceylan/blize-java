package com.blize.service;

import com.blize.document.Call;
import com.blize.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class MongoCallService {

    @Autowired
    private MongoTemplate mongoTemplate;

    public Call call(User user, User otherUser, boolean video) {

        Call call = new Call();
        call.setFrom(user.getId());
        call.setTo(otherUser.getId());
        call.setVideo(video);
        call.setDate(OffsetDateTime.now());

        return mongoTemplate.insert(call);
    }

    public Call accept(User user, String _id) {

        Query query = new Query()
                .addCriteria(Criteria.where("_id").is(_id))
                .addCriteria(Criteria.where("to").is(user.getId()));

        Update update = new Update().set("startDate", OffsetDateTime.now());
        return mongoTemplate.findAndModify(query, update, Call.class);
    }

    public Call close(User sessionUser, User otherUser, String id) {

        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(id));
        query.addCriteria(new Criteria().orOperator(
                new Criteria().andOperator(
                        Criteria.where("to").is(otherUser.getId()),
                        Criteria.where("from").is(sessionUser.getId())
                ),
                new Criteria().andOperator(
                        Criteria.where("from").is(otherUser.getId()),
                        Criteria.where("to").is(sessionUser.getId())
                )
        ));

        Call call = mongoTemplate.findOne(query, Call.class);

        if (call != null && call.getEndDate() == null) {
            call.setEndDate(OffsetDateTime.now());
            mongoTemplate.save(call);
        }

        return call;
    }
}
