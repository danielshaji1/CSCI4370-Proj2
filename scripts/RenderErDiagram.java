import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import javax.imageio.ImageIO;

/** Generates the physical ER diagram with Java's built-in graphics; no added library. */
public class RenderErDiagram {
    static Graphics2D g;
    static final Color INK = new Color(25, 43, 62), BLUE = new Color(226, 238, 249), GREEN = new Color(227, 243, 234);
    static void text(String text, int x, int y, int size, boolean bold) {
        g.setColor(INK); g.setFont(new Font("SansSerif", bold ? Font.BOLD : Font.PLAIN, size)); g.drawString(text, x, y);
    }
    static void entity(String name, int x, int y, String... fields) {
        int width=620, height=365;
        g.setColor(name.contains("collection") ? GREEN : BLUE);
        g.fill(new RoundRectangle2D.Double(x,y,width,height,18,18));
        g.setColor(new Color(118,147,172)); g.setStroke(new BasicStroke(2));
        g.draw(new RoundRectangle2D.Double(x,y,width,height,18,18));
        text(name,x+22,y+40,27,true);
        g.drawLine(x,y+56,x+width,y+56);
        for(int i=0;i<fields.length;i++) text(fields[i],x+22,y+88+i*32,21,false);
    }
    static void relation(int x, int y, String parent, String child, String fk) {
        int parentW=210, childX=x+405, childW=280;
        g.setColor(BLUE); g.fillRoundRect(x,y, parentW,52,12,12);
        g.setColor(child.contains("collection")?GREEN:BLUE); g.fillRoundRect(childX,y,childW,52,12,12);
        text(parent,x+12,y+34,23,true); text(child,childX+12,y+34,23,true);
        g.setColor(INK); g.setStroke(new BasicStroke(2)); g.drawLine(x+parentW,y+26,childX,y+26);
        // One parent to zero or many child records. Arrow points to the child.
        g.drawLine(childX-12,y+18,childX,y+26);g.drawLine(childX-12,y+34,childX,y+26);
        text("1",x+parentW+15,y+18,20,true); text("0..*",childX-62,y+18,20,true);
        text(fk,x+710,y+34,20,false);
    }
    public static void main(String[] args) throws Exception {
        BufferedImage image=new BufferedImage(2780,2200,BufferedImage.TYPE_INT_RGB);
        g=image.createGraphics();g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(Color.WHITE);g.fillRect(0,0,2780,2200);
        text("Microblogging platform | Physical ER diagram",60,65,40,true);
        text("PK = primary key   FK = foreign key   UQ = unique   All fields NOT NULL; timestamps default CURRENT_TIMESTAMP",60,110,24,false);
        entity("user",60,150,"PK userId INT AUTO_INCREMENT","UQ username VARCHAR(255)","password VARCHAR(255) [BCrypt]","firstName VARCHAR(255)","lastName VARCHAR(255)","CHECK: trimmed names / username >= 2");
        entity("post",740,150,"PK postId BIGINT AUTO_INCREMENT","FK userId INT -> user","content VARCHAR(500)","createdAt TIMESTAMP","CHECK: trimmed content is not empty","INDEX: userId, createdAt, postId");
        entity("comment",1420,150,"PK commentId BIGINT AUTO_INCREMENT","FK postId BIGINT -> post","FK userId INT -> user","content VARCHAR(500)","createdAt TIMESTAMP","CHECK: trimmed content is not empty","INDEX: postId, createdAt, commentId");
        entity("hashtag",2100,150,"PK hashtagId BIGINT AUTO_INCREMENT","UQ tag VARCHAR(100) [utf8mb4_bin]","CHECK: trimmed tag is not empty","Application stores lowercase tags");
        entity("follow",60,560,"PK / FK followerId INT -> user","PK / FK followedId INT -> user","createdAt TIMESTAMP","PK: (followerId, followedId)","CHECK: followerId <> followedId","FK actions: default RESTRICT");
        entity("post_like",740,560,"PK / FK userId INT -> user","PK / FK postId BIGINT -> post","createdAt TIMESTAMP","PK: (userId, postId)","One like per user per post");
        entity("bookmark",1420,560,"PK / FK userId INT -> user","PK / FK postId BIGINT -> post","createdAt TIMESTAMP","PK: (userId, postId)","One bookmark per user per post");
        entity("post_hashtag",2100,560,"PK / FK postId BIGINT -> post","PK / FK hashtagId BIGINT -> hashtag","PK: (postId, hashtagId)","INDEX: hashtagId, postId","Resolves post / hashtag many-to-many");
        entity("saved_collection",60,970,"PK collectionId BIGINT AUTO_INCREMENT","FK userId INT -> user","name VARCHAR(80)","description VARCHAR(255) DEFAULT ''","createdAt TIMESTAMP","UQ: (userId, name)","CHECK: trimmed name is not empty");
        entity("collection_post",740,970,"PK / FK collectionId BIGINT","    -> saved_collection","PK / FK postId BIGINT -> post","savedAt TIMESTAMP","PK: (collectionId, postId)","Resolves collection / post many-to-many");
        text("Design notes",1440,1015,29,true);
        text("All foreign keys reference the parent's primary key.",1440,1065,25,false);
        text("Each child row requires exactly one parent per FK.",1440,1110,25,false);
        text("A parent may have zero or many child rows (0..*).",1440,1155,25,false);
        text("ON DELETE CASCADE on every FK except follow.",1440,1200,25,false);
        text("Private collections enforce owner scope in all SQL operations.",1440,1245,25,false);
        text("Entities and junction tables store facts once; counts are derived.",1440,1290,25,false);
        text("Relationships (each arrow is one foreign key; role names distinguish the two follow relationships)",60,1410,28,true);
        String[][] relationships={
            {"user","post","post.userId"},{"user","comment","comment.userId"},{"post","comment","comment.postId"},
            {"user","follow","follow.followerId"},{"user","follow","follow.followedId"},
            {"user","post_like","post_like.userId"},{"post","post_like","post_like.postId"},
            {"user","bookmark","bookmark.userId"},{"post","bookmark","bookmark.postId"},
            {"post","post_hashtag","post_hashtag.postId"},{"hashtag","post_hashtag","post_hashtag.hashtagId"},
            {"user","saved_collection","saved_collection.userId"},
            {"saved_collection","collection_post","collection_post.collectionId"},{"post","collection_post","collection_post.postId"}
        };
        for(int i=0;i<relationships.length;i++){String[] r=relationships[i];relation(i<7?60:1440,1460+(i%7)*88,r[0],r[1],r[2]);}
        text("Schema source: schema.sql / database_setup.sql     |     MySQL 8.0.16+     |     Additional feature tables shown in green",60,2155,23,false);
        g.dispose(); ImageIO.write(image,"png",Path.of("ER_diagram.png").toFile());
    }
}
