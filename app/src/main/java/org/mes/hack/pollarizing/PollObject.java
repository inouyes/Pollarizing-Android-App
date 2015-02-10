package org.mes.hack.pollarizing;

import java.util.List;

public class PollObject {
    public static final int NO = 0;
    public static final int YES = 1;
    public static final int COMMENT = 2;

    public class Response {
        String user_id;
        String comment;
        int result;

        public Response(String user_id, String comment, int result) {
            this.user_id = user_id;
            this.comment = comment;
            this.result = result;
        }

        public String getUser_id() {
            return user_id;
        }

        public String getComment() {
            return comment;
        }

        public int getResult() {
            return result;
        }
    }

    String user_id;
    String poll_id;
    String caption;
    List<Response> responses;

    public PollObject(String user_id, String poll_id, String caption, List<Response> responses) {
        this.user_id = user_id;
        this.poll_id = poll_id;
        this.caption = caption;
        this.responses = responses;
    }

    public String getUser_id() {
        return user_id;
    }

    public String getPoll_id() {
        return poll_id;
    }

    public String getCaption() {
        return caption;
    }

    public List<Response> getResponses() {
        return responses;
    }

    public int getPerecentYes(){
        int numYes = 0;
        int numDown = 0;
        for(Response r : responses){
            if(r.getResult() == YES){
                numYes++;
            } else if (r.getResult() == NO){
                numDown++;
            }
        }
        int total = numYes + numDown;
        if(total == 0) return 0;
        else if (numYes == 0) return 0;
        else if (numDown == 0) return 100;
        else return (int)((float) numYes / (float) total);
    }

    public int getPercentDown(){
        int numYes = 0;
        int numDown = 0;
        for(Response r : responses){
            if(r.getResult() == YES){
                numYes++;
            } else if (r.getResult() == NO){
                numDown++;
            }
        }
        int total = numYes + numDown;
        if(total == 0) return 0;
        else if (numYes == 0) return 100;
        else if (numDown == 0) return 0;
        else return (int)((float) numDown / (float) total);
    }
}
