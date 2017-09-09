package org.study.kirill.Exeptions;


   public class WorkerThreadExсeption extends Exception {
        private Runnable task;

        public WorkerThreadExсeption() {
            super();
        }
        public WorkerThreadExсeption(String message) {
            super(message);
        }
        public WorkerThreadExсeption(String message, Throwable cause) {
            super(message, cause);
        }
        public WorkerThreadExсeption(Throwable cause) {
            super(cause);
        }
        public WorkerThreadExсeption(String message, Runnable task){
            super(message);
            this.task = task;
        }


        public Runnable getTask() {return task;}
    }
