package HistoryRecycleView;

public class HistoryObject {
        private String cleanId;
        private String time;

        public HistoryObject(String cleanId, String time){
            this.cleanId = cleanId;
            this.time = time;
        }

        public String getCleanId(){return cleanId;}
        public void setCleanId(String cleanId) {
            this.cleanId = cleanId;
        }

        public String getTime(){return time;}
        public void setTime(String time) {
            this.time = time;
        }
}
