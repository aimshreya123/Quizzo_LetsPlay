package com.marsinnovations.letsplay;

import com.google.firebase.firestore.DocumentId;

public class QuizListModel {

    @DocumentId
    private String quiz_id;
    private String Name, Description, Level, Image, Visibility;
    private long Questions;

    public QuizListModel(){}

    public QuizListModel(String quiz_id, String name, String desc, String level, String image, String visibility, long ques) {
        this.quiz_id = quiz_id;
        this.Name = name;
        this.Description = desc;
        this.Level = level;
        this.Image = image;
        this.Visibility = visibility;
        this.Questions = ques;
    }

    public String getQuiz_id() {
        return quiz_id;
    }

    public void setQuiz_id(String quiz_id) {
        this.quiz_id = quiz_id;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        this.Name = name;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String desc) {
        this.Description = desc;
    }

    public String getLevel() {
        return Level;
    }

    public void setLevel(String level) {
        this.Level = level;
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        this.Image = image;
    }

    public String getVisibility() {
        return Visibility;
    }

    public void setVisibility(String visibility) {
        this.Visibility = visibility;
    }

    public long getQuestions() {
        return Questions;
    }

    public void setQuestions(long ques) {
        this.Questions = ques;
    }
}
