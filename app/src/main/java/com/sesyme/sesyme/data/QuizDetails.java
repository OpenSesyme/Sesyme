package com.sesyme.sesyme.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class QuizDetails {

    private Date releaseQuiz;
    private Date releaseAnswers;
    private String clusterId;
    private String creator;
    private String title;
    private String question;
    private List<String> options;
    private List<String> option1;
    private List<String> option2;
    private List<String> option3;
    private List<String> option4;
    private List<String> attempted;
    private String answer;

    //Do not delete
    public QuizDetails() {
    }

    public QuizDetails(Date releaseQuiz, Date releaseAnswers, String clusterId, String creator, String title) {
        this.releaseQuiz = releaseQuiz;
        this.releaseAnswers = releaseAnswers;
        this.clusterId = clusterId;
        this.creator = creator;
        this.title = title;
        this.attempted = new ArrayList<>();
    }

    public QuizDetails(String question, List<String> options, String answer) {
        this.question = question;
        this.options = options;
        this.option1 = new ArrayList<>();
        this.option2 = new ArrayList<>();
        this.option3 = new ArrayList<>();
        this.option4 = new ArrayList<>();
        this.answer = answer;
    }

    public Date getReleaseQuiz() {
        return releaseQuiz;
    }

    public Date getReleaseAnswers() {
        return releaseAnswers;
    }

    public String getClusterId() {
        return clusterId;
    }

    public String getCreator() {
        return creator;
    }

    public String getTitle() {
        return title;
    }

    public String getQuestion() {
        return question;
    }

    public List<String> getOptions() {
        return options;
    }

    public List<String> getOption1() {
        return option1;
    }

    public List<String> getOption2() {
        return option2;
    }

    public List<String> getOption3() {
        return option3;
    }

    public List<String> getOption4() {
        return option4;
    }

    public String getAnswer() {
        return answer;
    }

    public List<String> getAttempted() {
        return attempted;
    }
}
