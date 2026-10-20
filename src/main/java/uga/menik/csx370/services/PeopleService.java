/**
Copyright (c) 2026 Sami Menik, PhD. All rights reserved.

This is a project developed by Dr. Menik to give the students an opportunity to apply database concepts learned in the class in a real world project. Permission is granted to host a running version of this software and to use images or videos of this work solely for the purpose of demonstrating the work to potential employers. Any form of reproduction, distribution, or transmission of the software's source code, in part or whole, without the prior written consent of the copyright owner, is strictly prohibited.
*/
package uga.menik.csx370.services;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import uga.menik.csx370.db.SqlQueries;
import uga.menik.csx370.models.FollowableUser;
import uga.menik.csx370.utility.PostText;

@Service
public class PeopleService {
    private final JdbcTemplate jdbc;
    public PeopleService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public List<FollowableUser> getFollowableUsers(String viewer) {
        return jdbc.query(SqlQueries.PEOPLE, (row, index) -> new FollowableUser(
                row.getString("userId"), row.getString("firstName"), row.getString("lastName"),
                row.getBoolean("followed"), PostText.date(row.getTimestamp("lastPost"))), viewer, viewer);
    }

    public void follow(String viewer, String target, boolean add) {
        if (viewer.equals(target)) throw new IllegalArgumentException("You cannot follow yourself.");
        jdbc.update(add ? SqlQueries.FOLLOW : SqlQueries.UNFOLLOW, viewer, target);
    }
}
