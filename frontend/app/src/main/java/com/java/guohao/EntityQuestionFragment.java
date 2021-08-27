package com.java.guohao;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.switchmaterial.SwitchMaterial;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class EntityQuestionFragment extends Fragment {
    private static class SafeHandler extends Handler {
        private final WeakReference<Fragment> mParent;
        public SafeHandler(Fragment parent) {
            mParent = new WeakReference<>(parent);
        }
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            EntityQuestionFragment parent = (EntityQuestionFragment) mParent.get();
            if (msg.what == GlobVar.SUCCESS_FROM_INTERNET) {
                String data = msg.obj.toString();
                parent.parseData(data);
                Storage.save(parent.requireContext(), parent.mStorageKey, data);
            }
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView mCard;
        private final TextView mText;
        private final ImageView mIcon;

        public ViewHolder(View view) {
            super(view);
            mCard = view.findViewById(R.id.entity_question_choice_card);
            mText = view.findViewById(R.id.entity_question_choice_text);
            mIcon = view.findViewById(R.id.entity_question_choice_status);
        }

        public MaterialCardView getCard() {
            return mCard;
        }

        public TextView getText() {
            return mText;
        }

        public ImageView getIcon() {
            return mIcon;
        }
    }

    private class Question {
        public static final int UNANSWERED = -1;
        public Integer id;
        public Integer answer;
        public String question;
        public ArrayList<String> choices;
        public Integer myAnswer;
        public Question() {
            choices = new ArrayList<>();
            myAnswer = UNANSWERED;
        }
    }

    private static HashMap<String, String> mChoiceMap;
    static {
        mChoiceMap = new HashMap<>();
        mChoiceMap.put("①", "A.");
        mChoiceMap.put("②", "B.");
        mChoiceMap.put("③", "C.");
        mChoiceMap.put("④", "D.");
    }

    private SafeHandler mHandler = new SafeHandler(this);
    private String mStorageKey;
    private String mLabel;
    private String mCourse;
    private String mUri;
    private Question mLocalDataset;
    private ArrayList<Question> mAllDataset;
    private Integer currentQuestion;
    RecyclerView mView;
    TextView mQuestion;
    TextView mNum;
    CircularProgressIndicator mLoading;
    Button mPrev;
    Button mNext;
    SwitchMaterial mIsShowAnswer;
    RecyclerView.Adapter<ViewHolder> mAdapter;

    public EntityQuestionFragment() {
        this("", "", "");
    }

    public EntityQuestionFragment(String label, String course, String uri) {
        mLabel = label;
        mCourse = course;
        mUri = uri;
        currentQuestion = 0;
        mLocalDataset = new Question();
        mAllDataset = new ArrayList<>();
        mStorageKey = Storage.getKey(this.getClass().getSimpleName(), label, course);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_entity_question, container, false);
        mLoading = view.findViewById(R.id.entity_question_loading);
        mQuestion = view.findViewById(R.id.entity_question_question);
        mIsShowAnswer = view.findViewById(R.id.entity_question_show_answer);
        mIsShowAnswer.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mAdapter.notifyItemRangeRemoved(0, mLocalDataset.choices.size());
            mAdapter.notifyItemRangeInserted(0, mLocalDataset.choices.size());
        });
        mNum = view.findViewById(R.id.entity_question_num);
        mPrev = view.findViewById(R.id.entity_question_prev);
        mNext = view.findViewById(R.id.entity_question_next);
        mPrev.setOnClickListener(v -> setCurrentQuestion(currentQuestion - 1));
        mNext.setOnClickListener(v -> setCurrentQuestion(currentQuestion + 1));


        mView = view.findViewById(R.id.entity_question_choice_view);
        mView.setLayoutManager(new LinearLayoutManager(requireContext()));
        mAdapter = new RecyclerView.Adapter<ViewHolder>() {

            @Override
            public long getItemId(int position) {
                return mLocalDataset.choices.get(position).toString().hashCode();
            }

            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(requireContext()).inflate(R.layout.card_entity_question_choice, parent, false);
                ViewHolder h = new ViewHolder(view);
                MaterialCardView c = h.getCard();
                c.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // correct
                        if (mLocalDataset.myAnswer == Question.UNANSWERED) {
                            mLocalDataset.myAnswer = h.getBindingAdapterPosition();
                            mAdapter.notifyItemRangeRemoved(0, mLocalDataset.choices.size());
                            mAdapter.notifyItemRangeInserted(0, mLocalDataset.choices.size());
                        }
                    }
                });
                return h;
            }

            @Override
            public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
                TextView tv = holder.getText();
                tv.setText(mLocalDataset.choices.get(position));
                ImageView icon = holder.getIcon();
                if (mLocalDataset.myAnswer != Question.UNANSWERED || mIsShowAnswer.isChecked()) {
                    // display correct answer
                    if (holder.getBindingAdapterPosition() == mLocalDataset.answer) {
                        Helper.ImageViewFadeIn(requireContext(), icon, R.drawable.check, 200);
                    } else if (holder.getBindingAdapterPosition() == mLocalDataset.myAnswer) {
                        Helper.ImageViewFadeIn(requireContext(), icon, R.drawable.delete, 200);
                    } else {
                        icon.setImageResource(0);
                    }
                } else {
                    icon.setImageResource(0);
                }
            }

            @Override
            public int getItemCount() {
                return mLocalDataset.choices.size();
            }
        };
        mView.setAdapter(mAdapter);
        ((SimpleItemAnimator) mView.getItemAnimator()).setSupportsChangeAnimations(false);
        initData();
        return view;
    }

    private void parseData(String raw_data) {
        Log.i("EntityQuestionFragment", raw_data);
        try {
            mAllDataset.clear();
            JSONArray data = new JSONObject(raw_data).getJSONArray("data");
            for (int i = 0; i < data.length(); ++i) {
                Question q = new Question();
                JSONObject item = data.getJSONObject(i);
                System.out.println(item);
                q.id = item.getInt("id");
                String answerStr = item.getString("qAnswer"); // fucking quan 1
                String body = item.getString("qBody");
                for (Map.Entry<String, String> e : mChoiceMap.entrySet()) {
                    answerStr = answerStr.replace(e.getKey(), e.getValue());
                }
                if (!answerStr.equals(item.getString("qAnswer"))) {
                    for (Map.Entry<String, String> e : mChoiceMap.entrySet()) {
                        body = body.replace(e.getKey(), e.getValue());
                    }
                }
                q.answer = answerStr.charAt(0) - 'A';
                // get question and choice
                ArrayList<Integer> splitIndex = new ArrayList<>();
                splitIndex.add(0);
                for (char c = 'A'; c <= 'D'; ++c) {
                    splitIndex.add(body.indexOf(c + "."));
                }
                splitIndex.add(body.length());
                q.question = body.substring(splitIndex.get(0), splitIndex.get(1));
                for (int j = 1; j < splitIndex.size() - 1; ++j) {
                    q.choices.add(body.substring(splitIndex.get(j), splitIndex.get(j + 1)));
                }
                mAllDataset.add(q);
            }
            setCurrentQuestion(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mLoading.setVisibility(View.GONE);
    }

    private void initData() {
        if (Storage.contains(requireContext(), mStorageKey)) {
            parseData(Storage.load(requireContext(), mStorageKey));
        } else {
            onRefresh();
        }
    }

    private void setCurrentQuestion(int index) {
        if (index < 0 || index >= mAllDataset.size()) {
            return;
        }

        currentQuestion = index;
        mAdapter.notifyItemRangeRemoved(0, mLocalDataset.choices.size());
        mLocalDataset = mAllDataset.get(index);
        mNum.setText(String.format("%d/%d", index + 1, mAllDataset.size()));
        mQuestion.setText(mLocalDataset.question);
        mAdapter.notifyItemRangeInserted(0, mLocalDataset.choices.size());
    }

    // get data from Internet
    private void onRefresh() {
        HttpUtils.getQuestion(mCourse, mLabel, mHandler);
    }
}