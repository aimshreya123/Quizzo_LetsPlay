package com.marsinnovations.letsplay;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class QuizFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "QUIZ_FRAGMENT_LOG";

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private NavController navController;

    private String currentUserId;

    private String quizName;
    private String quizId;

    //Declaring UI Elements
    private TextView quizTitle;
    private TextView prepMessage;
    private TextView quesText;
    private TextView quesNum;
    private TextView quesTimer;
    private Button option1;
    private Button option2;
    private Button option3;
    private Button nextQuestion;
    private ImageButton closeQuiz;
    private ProgressBar quesTimerProg;



    private List<QuestionsModel> allQuestionsList = new ArrayList<>();
    private List<QuestionsModel> questionsToAnswer = new ArrayList<>();
    private long totalQuestionsToAnswer = 0L;
    private CountDownTimer countDownTimer;

    private boolean canAnswer = false;
    private int currentQuestion = 0;

    private int correctAnswers = 0;
    private int wrongAnswers = 0;
    private int notAnswered = 0;

    public QuizFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_quiz, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);

        firebaseAuth = FirebaseAuth.getInstance();

        if(firebaseAuth.getCurrentUser() != null){
            currentUserId = firebaseAuth.getCurrentUser().getUid();
        }else{

        }

        firebaseFirestore = FirebaseFirestore.getInstance();

        quizTitle = view.findViewById(R.id.loadingTxt);
        prepMessage = view.findViewById(R.id.verifyMsg);
        quesText = view.findViewById(R.id.prepMsg);
        quesNum = view.findViewById(R.id.quesNumber);
        quesTimer = view.findViewById(R.id.prepTimer);
        option1 = view.findViewById(R.id.optionbtn1);
        option2 = view.findViewById(R.id.optionbtn2);
        option3 = view.findViewById(R.id.optionbtn3);
        nextQuestion = view.findViewById(R.id.nextQuesbtn);
        closeQuiz = view.findViewById(R.id.closeButton);
        quesTimerProg = view.findViewById(R.id.progressBar);
        quizId = QuizFragmentArgs.fromBundle(getArguments()).getQuizId();

        quizName = QuizFragmentArgs.fromBundle(getArguments()).getQuizName();
        totalQuestionsToAnswer = QuizFragmentArgs.fromBundle(getArguments()).getTotalQuestions();


        firebaseFirestore.collection("QuizList")
                .document(quizId).collection("Questions")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){

                    allQuestionsList = task.getResult().toObjects(QuestionsModel.class);
                    pickQuestions();
                    loadUI();

                }else{
                   quizTitle.setText("Error Loading Data..!");
                }
            }
        });

        option1.setOnClickListener(this);
        option2.setOnClickListener(this);
        option3.setOnClickListener(this);

        nextQuestion.setOnClickListener(this);
    }

    private void loadUI() {
        //Quiz Data Loaded, Load the UI
        quizTitle.setText("Quizzo - Let's Play");
        quesText.setText("Load First Question");

        enableOptions();

        loadQuestion(1);
    }

    private void loadQuestion(int questNumber) {

        quesNum.setText(questNumber+"");

        //Load Questions
        quesText.setText(questionsToAnswer.get(questNumber-1).getQuestion());

        option1.setText(questionsToAnswer.get(questNumber-1).getOption_a());
        option2.setText(questionsToAnswer.get(questNumber-1).getOption_b());
        option3.setText(questionsToAnswer.get(questNumber-1).getOption_c());

        canAnswer = true;
        currentQuestion = questNumber;

        //Start Timer
        startTimer(questNumber);
    }

    private void startTimer(int questionNumber) {

        final Long timeToAnswer = questionsToAnswer.get(questionNumber-1).getTimer();
        quesTimer.setText(timeToAnswer.toString());

        quesTimerProg.setVisibility(View.VISIBLE);

        //Start CountDown
        countDownTimer = new CountDownTimer(timeToAnswer*1000,10){

            @Override
            public void onTick(long millisUntilFinished) {
                quesTimer.setText(millisUntilFinished/1000 +"");

                Long percent = millisUntilFinished/(timeToAnswer*10);
                quesTimerProg.setProgress(percent.intValue());
            }

            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onFinish() {
                canAnswer = false;

                prepMessage.setText("Time Up!!!");
                prepMessage.setTextColor(getResources().getColor(R.color.colorPrimary, null));
                notAnswered++;
                showNextBtn();
            }
        };

        countDownTimer.start();
    }

    private void enableOptions() {
        //Visibility of options
        option1.setVisibility(View.VISIBLE);
        option2.setVisibility(View.VISIBLE);
        option3.setVisibility(View.VISIBLE);

        //Enable option buttons
        option1.setEnabled(true);
        option2.setEnabled(true);
        option3.setEnabled(true);

        //Hide Preparing Messages
        prepMessage.setVisibility(View.INVISIBLE);
        nextQuestion.setVisibility(View.INVISIBLE);
        nextQuestion.setEnabled(false);
    }

    private void pickQuestions() {
        for (int i=0; i< totalQuestionsToAnswer; i++){
            int randomNumber = getRandomInteger(allQuestionsList.size(),0);
            questionsToAnswer.add(allQuestionsList.get(randomNumber));
            allQuestionsList.remove(randomNumber);
        }
    }

    public static int getRandomInteger(int maximum, int minimum){
        return ((int) (Math.random()*(maximum - minimum)))+ minimum;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.optionbtn1:
                verifyAnswer(option1);
                break;
            case R.id.optionbtn2:
                verifyAnswer(option2);
                break;
            case R.id.optionbtn3:
                verifyAnswer(option3);
                break;
            case R.id.nextQuesbtn:
                if(currentQuestion == totalQuestionsToAnswer){
                 submitResults();
            }else{
                currentQuestion++;
                loadQuestion(currentQuestion);
                resetOptions();

            }
                break;
        }
    }

    private void submitResults() {
        HashMap<String, Object> resultMap = new HashMap<>();
        resultMap.put("correct",correctAnswers);
        resultMap.put("wrong",wrongAnswers);
        resultMap.put("unanswered",notAnswered);

        firebaseFirestore.collection("QuizList")
                .document(quizId).collection("Results")
                .document(currentUserId).set(resultMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    QuizFragmentDirections.ActionQuizFragmentToResultFragment action = QuizFragmentDirections.actionQuizFragmentToResultFragment();
                    action.setQuizId(quizId);
                    navController.navigate(action);
                }else{
                    quizTitle.setText(task.getException().getMessage());
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void resetOptions() {
        option1.setBackground(getResources().getDrawable(R.drawable.option_btn_bg, null));;
        option2.setBackground(getResources().getDrawable(R.drawable.option_btn_bg, null));;
        option3.setBackground(getResources().getDrawable(R.drawable.option_btn_bg, null));;

        option1.setTextColor(getResources().getColor(R.color.LightText, null));
        option2.setTextColor(getResources().getColor(R.color.LightText, null));
        option3.setTextColor(getResources().getColor(R.color.LightText, null));

        prepMessage.setVisibility(View.INVISIBLE);
        nextQuestion.setVisibility(View.INVISIBLE);
        nextQuestion.setEnabled(false);
    }

    @SuppressLint("NewApi")
    private void verifyAnswer(Button selectedAnswerBtn) {

        selectedAnswerBtn.setTextColor(getResources().getColor(R.color.colorPrimaryDark, null));

        if(canAnswer){
            if(questionsToAnswer.get(currentQuestion-1).getAnswer().equals(selectedAnswerBtn.getText())){
                correctAnswers++;
                selectedAnswerBtn.setBackground(getResources().getDrawable(R.drawable.correct_answer_btn_bg));
                prepMessage.setText("Correct Answer");
                prepMessage.setTextColor(getResources().getColor(R.color.colorPrimary, null));

            }else{
                wrongAnswers++;
                selectedAnswerBtn.setBackground(getResources().getDrawable(R.drawable.wrong_answer_btn_bg));
                prepMessage.setText("Wrong Answer");
                prepMessage.setTextColor(getResources().getColor(R.color.colorAccent, null));
            }
            canAnswer = false;

            countDownTimer.cancel();

            showNextBtn();
        }
    }

    private void showNextBtn() {
        if(currentQuestion == totalQuestionsToAnswer){
            nextQuestion.setText("Submit Quiz");
        }
        prepMessage.setVisibility(View.VISIBLE);
        nextQuestion.setVisibility(View.VISIBLE);
        nextQuestion.setEnabled(true);
    }

}