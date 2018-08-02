package beaver;

import jodd.json.JsonObject;
import jodd.json.JsonParser;
import jodd.util.StringUtil;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;

@Entity // This tells Hibernate to make a table out of this class
public class Stacks {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer stackid;

    private String stackname;

    private String stackarn;

    private String username;

    private String parameters;

    private Integer templateid;

    private String availableregion;

    private String comments;

    private String status;

    private String stackoutputs;

    private String stackresources;

    private String stackevents;

    private Date createdat;

    private Date updatedat;

    public String toString() {
        return String.format("%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s",
                stackid, stackname, stackarn, username, parameters, templateid, availableregion, comments, status, stackoutputs,
                stackresources, stackevents, DICT.DF_YMDHMS.format(createdat), DICT.DF_YMDHMS.format(updatedat));
    }

    public String toJsonStr() {
        JsonObject json = new JsonObject();
        json.put("stackid", stackid);
        json.put("stackname", stackname);
        json.put("stackarn", stackarn);

        if (!StringUtil.isEmpty(parameters))
            json.put("parameters", JsonParser.create().parseAsJsonObject(parameters));
        else
            json.put("parameters", "");

        json.put("templateid", templateid);
        json.put("availableregion", availableregion);
        json.put("comments", comments);
        json.put("status", status);

        if (!StringUtil.isEmpty(stackoutputs))
            json.put("stackoutputs", JsonParser.create().parseAsJsonArray(stackoutputs));
        else
            json.put("stackoutputs", "");

        if (!StringUtil.isEmpty(stackresources))
            json.put("stackresources", JsonParser.create().parseAsJsonArray(stackresources));
        else
            json.put("stackresources", "");

        if (!StringUtil.isEmpty(stackevents))
            json.put("stackevents", JsonParser.create().parseAsJsonArray(stackevents));
        else
            json.put("stackevents", "");

        json.put("createdat", DICT.DF_YMDHMS.format(createdat));
        json.put("updatedat", DICT.DF_YMDHMS.format(updatedat));
        return json.toString();
    }

    public String getStackarn() {
        return stackarn;
    }

    public void setStackarn(String stackarn) {
        this.stackarn = stackarn;
    }

    public Integer getStackid() {
        return stackid;
    }

    public void setStackid(Integer stackid) {
        this.stackid = stackid;
    }

    public String getStackname() {
        return stackname;
    }

    public void setStackname(String stackname) {
        this.stackname = stackname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public Integer getTemplateid() {
        return templateid;
    }

    public void setTemplateid(Integer templateid) {
        this.templateid = templateid;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getStackresources() {
        return stackresources;
    }

    public void setStackresources(String stackresources) {
        this.stackresources = stackresources;
    }

    public String getStackevents() {
        return stackevents;
    }

    public void setStackevents(String stackevents) {
        this.stackevents = stackevents;
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

    public String getAvailableregion() {
        return availableregion;
    }

    public void setAvailableregion(String availableregion) {
        this.availableregion = availableregion;
    }

    public String getStackoutputs() {
        return stackoutputs;
    }

    public void setStackoutputs(String stackoutputs) {
        this.stackoutputs = stackoutputs;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}