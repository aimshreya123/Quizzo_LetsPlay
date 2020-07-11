package com.marsinnovations.letsplay;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;


public class QuizDetailFragment extends Fragment implements View.OnClickListener {

    private QuizListViewModel quizListViewModel;
    private NavController navController;
    private FirebaseFirestore firebaseFirestore;
    private int position;

    private String currentUserId;

    private TextView resultCorrect;
    private TextView resultWrong;
    private TextView resultMissed;

    private ImageView detailImageView;
    private TextView detailQuizTitle;
    private TextView detailQuizDesc;
    private TextView detailLevel;
    private TextView detailTotalQues;
    private TextView detailLastScore;
    private Button startQuizBtn;
    private String quizId;
    private long totalQuestions = 0;

    public QuizDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_quiz_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);

        position = QuizDetailFragmentArgs.fromBundle(getArguments()).getPosition();

        firebaseFirestore = FirebaseFirestore.getInstance();

        detailImageView = view.findViewById(R.id.quizpgImage);
        detailQuizTitle = view.findViewById(R.id.quizpgTitle);
        detailQuizDesc = view.findViewById(R.id.quizpgDescription);
        detailTotalQues = view.findViewById((R.id.totalQues));
        detailLevel = view.findViewById(R.id.difficulty);
        detailLastScore = view.findViewById(R.id.lastScore);
        startQuizBtn = view.findViewById(R.id.startQuiz);
        startQuizBtn.setOnClickListener(this);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        quizListViewModel = new ViewModelProvider(getActivity()).get(QuizListViewModel.class);
        quizListViewModel.getQuizListModelData().observe(getViewLifecycleOwner(), new Observer<List<QuizListModel>>() {
            @Override
            public void onChanged(List<QuizListModel> quizListModels) {

                detailQuizTitle.setText(quizListModels.get(position).getName());
                detailQuizDesc.setText(quizListModels.get(position).getDescription());
                detailTotalQues.setText(quizListModels.get(position).getQuestions()+"");
                detailLevel.setText(quizListModels.get(position).getLevel());
                String imageUrl = quizListModels.get(position).getImage();

                Glide.with(getContext())
                        .load(quizListModels.get(position).getImage())
                        .centerCrop()
                        .placeholder(R.drawable.placeholder_image)
                        .into(detailImageView);

                quizId = quizListModels.get(position).getQuiz_id();
                totalQuestions = quizListModels.get(position).getQuestions();

                //loadResultsData();
            }
        });






    }

    private void loadResultsData() {
        firebaseFirestore.collection("QuizList")
                .document(quizId).collection("Results")
                .document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot result = task.getResult();

                    Long correct = result.getLong("correct");
                    Long wrong = result.getLong("wrong");
                    Long missed = result.getLong("unanswered");

                    Long total = correct+wrong+missed;
                    Long percent = (correct*100)/total;

                    resultCorrect.setText(correct.toString());
                    resultWrong.setText(wrong.toString());
                    resultMissed.setText(missed.toString());

                }else{

                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.startQuiz:
                QuizDetailFragmentDirections.ActionQuizDetailFragmentToQuizFragment action = QuizDetailFragmentDirections.actionQuizDetailFragmentToQuizFragment();
                action.setTotalQuestions(totalQuestions);
                action.setQuizId(quizId);
                navController.navigate(action);
                break;
        }
    }
}