package beaver;

import jodd.json.JsonObject;
import jodd.util.StringUtil;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity // This tells Hibernate to make a table out of this class
@Table(name = "stacklogs")
public class StackLogs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer stacklogid;

    private Integer stackid;

    private String username;

    private String comments;

    private String status;

    private Date createdat;

    private Date updatedat;

    public String toString() {
        return String.format("%s, %s, %s, %s, %s, %s, %s",
                stacklogid, stackid, username, status, comments, DICT.DF_YMDHMS.format(createdat), DICT.DF_YMDHMS.format(updatedat));
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.put("stacklogid", stacklogid);
        json.put("stackid", stackid);
        json.put("username", username);
        json.put("status", status);
        json.put("comments", !StringUtil.isEmpty(comments) ? comments : "");
        json.put("createdat", DICT.DF_YMDHMS.format(createdat));
        json.put("updatedat", DICT.DF_YMDHMS.format(updatedat));
        return json;
    }


    public Integer getStacklogid() {
        return stacklogid;
    }

    public void setStacklogid(Integer stacklogid) {
        this.stacklogid = stacklogid;
    }

    public Integer getStackid() {
        return stackid;
    }

    public void setStackid(Integer stackid) {
        this.stackid = stackid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreatedat() {
        return createdat;
    }

    public void setCreatedat(Date createdat) {
        this.createdat = createdat;
    }

    public Date getUpdatedat() {
        return updatedat;
    }

    public void setUpdatedat(Date updatedat) {
        this.updatedat = updatedat;
    }
}