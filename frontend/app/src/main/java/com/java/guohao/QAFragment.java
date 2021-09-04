package com.java.guohao;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chivorn.smartmaterialspinner.SmartMaterialSpinner;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;

public class QAFragment extends Fragment {

    private static class SafeHandler extends Handler {
        private final WeakReference<QAFragment> mParent;
        public SafeHandler(QAFragment parent) {
            mParent = new WeakReference<>(parent);
        }
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            String text = "系统异常，请稍后重试";
            QAFragment parent = mParent.get();
            try {
                JSONObject obj = new JSONObject(msg.obj.toString());
                System.out.println(obj);
                JSONObject data = obj.getJSONArray("data").getJSONObject(0);
                System.out.println(data);
                String subject = data.getString("subject");
                String score = String.valueOf(data.getDouble("score"));
                String answer = data.getString("value");
                if (answer.isEmpty()) {
                    text = data.getString("message");
                } else {
                    text = "相关实体: " + subject + "\n" + "分数: " + score + "\n" + "答案: " + answer;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            parent.mLocalDataset.add(new QAMessage(QAMessage.ROBOT, "", text));
            parent.mAdapter.notifyItemInserted(parent.mLocalDataset.size() - 1);
        }
    }

    private SafeHandler mHandler = new SafeHandler(this);
    
    RecyclerView mView;
    EditText mQuestion;
    TextInputLayout mQuestionLayout;
    SmartMaterialSpinner<String> mCourse;
    FloatingActionButton mSubmit;
    RecyclerView.Adapter<RecyclerView.ViewHolder> mAdapter;

    public static class QAMessage {
        public static final int USER = 0;
        public static final int ROBOT = 1;
        public int from;
        public String course;
        public String text;
        public QAMessage(int from, String course, String text) {
            this.from = from;
            this.course = course;
            this.text = text;
        }
    }

    ArrayList<QAMessage> mLocalDataset;
    

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mLocalDataset = new ArrayList<>();
        View view = inflater.inflate(R.layout.fragment_q_a, container, false);
        mView = view.findViewById(R.id.qa_view);
        mQuestion = view.findViewById(R.id.qa_question);
        mQuestionLayout = view.findViewById(R.id.qa_input);
        mSubmit = view.findViewById(R.id.qa_button);
        mCourse = view.findViewById(R.id.qa_course);
        mCourse.setItem(Arrays.asList(GlobVar.SUBJECTS));
        mSubmit.setOnClickListener(v -> {
            String question = mQuestion.getText().toString();
            if (question.isEmpty()) return;
            String course = mCourse.getSelectedItem();
            mQuestion.setText("");
            mLocalDataset.add(new QAMessage(QAMessage.USER, course, question));
            mAdapter.notifyItemInserted(mLocalDataset.size() - 1);
            HttpUtils.getAnswer(course, question, mHandler);
        });

        mAdapter = new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            class ViewHolder extends RecyclerView.ViewHolder {
                private final TextView text;
                ViewHolder(View view) {
                    super(view);
                    text = view.findViewById(R.id.qa_text);
                }

                TextView getText() {
                    return text;
                }
            }

            @Override
            public int getItemViewType(int position) {
                return mLocalDataset.get(position).from;
            }

            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                switch (viewType) {
                    case QAMessage.USER : return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.card_qa_user_message, parent, false));
                    case QAMessage.ROBOT : return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.card_qa_bot_message, parent, false));
                }
                return null;
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                ViewHolder h = (ViewHolder) holder;
                h.getText().setText(mLocalDataset.get(position).text);
            }

            @Override
            public int getItemCount() {
                return mLocalDataset.size();
            }
        };
        mView.setLayoutManager(new LinearLayoutManager(requireContext()));
        mView.setAdapter(mAdapter);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}