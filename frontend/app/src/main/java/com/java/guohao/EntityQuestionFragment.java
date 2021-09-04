package com.java.guohao;

import android.annotation.SuppressLint;
import android.os.Bundle;
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

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.switchmaterial.SwitchMaterial;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EntityQuestionFragment extends Fragment {
    private static class SafeHandler extends Handler {
        private final WeakReference<Fragment> mParent;
        private int cur;
        public SafeHandler(Fragment parent, int start) {
            mParent = new WeakReference<>(parent);
            cur = start;
        }
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            EntityQuestionFragment parent = (EntityQuestionFragment) mParent.get();
            if (msg.what == GlobVar.SUCCESS_FROM_INTERNET) {
                String data = msg.obj.toString();
                parent.parseData(data);
                Storage.save(parent.requireContext(), parent.mStorageKey[cur++], data);
                if (cur == 1) {
                    parent.mLoading.setVisibility(View.GONE);
                    parent.setCurrentQuestion(0);
                }
                while (cur < parent.mUri.length && Storage.contains(parent.requireContext(), parent.mStorageKey[cur])) {
                    ++cur;
                }
                if (cur < parent.mUri.length) {
                    HttpUtils.getQuestion(parent.mCourse[cur], parent.mLabel[cur], parent.mHandler);
                } else {
                    parent.loadingAccomplished = true;
                    parent.setCurrentQuestion(parent.currentQuestion);
                }
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

    private static class Question {
        public static final int UNANSWERED = -1;
        public Integer id;
        public Integer answer;
        public String question;
        public ArrayList<String> choices;
        public Integer myAnswer;
        public Question() {
            choices = new ArrayList<>();
            myAnswer = UNANSWERED;
            answer = 4;
        }

        public String answerStr() {
            return TextUtils.join("\n", choices);
        }
    }

    private static final HashMap<String, String> mChoiceMap;
    static {
        mChoiceMap = new HashMap<>();
        mChoiceMap.put("①", "A.");
        mChoiceMap.put("②", "B.");
        mChoiceMap.put("③", "C.");
        mChoiceMap.put("④", "D.");
    }

    private SafeHandler mHandler;
    private final String[] mStorageKey;
    private final String[] mLabel;
    private final String[] mCourse;
    private final String[] mUri;
    private Question mLocalDataset;
    private ArrayList<Question> mAllDataset;
    private Integer currentQuestion;
    private Double score;
    private boolean loadingAccomplished = false;
    RecyclerView mView;
    TextView mQuestion;
    TextView mNum;
    TextView mScore;
    CircularProgressIndicator mLoading;
    Button mPrev;
    Button mNext;
    Button mShare;
    SwitchMaterial mIsShowAnswer;
    RecyclerView.Adapter<ViewHolder> mAdapter;
    WbShareInterface mShareInterface;

    public EntityQuestionFragment() {
        this("", "", "", null);
    }

    public EntityQuestionFragment(String[] label, String[] course, String[] uri, WbShareInterface shareInterface) {
        mLabel = label.clone();
        mCourse = course.clone();
        mUri = uri.clone();
        mStorageKey = new String[mUri.length];
        for (int i = 0; i < mUri.length; ++i) {
            mStorageKey[i] = Storage.getKey(this.getClass().getSimpleName(), label[i], course[i]);
        }
        mShareInterface = shareInterface;
        currentQuestion = 0;
        score = 0.0;
    }

    public EntityQuestionFragment(String label, String course, String uri, WbShareInterface shareInterface) {
        this(new String[]{label}, new String[]{course}, new String[]{uri}, shareInterface);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mLocalDataset = new Question();
        mAllDataset = new ArrayList<>();
        View view = inflater.inflate(R.layout.fragment_entity_question, container, false);
        mLoading = view.findViewById(R.id.entity_question_loading);
        mQuestion = view.findViewById(R.id.entity_question_question);
        mIsShowAnswer = view.findViewById(R.id.entity_question_show_answer);
        mIsShowAnswer.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (mLocalDataset.myAnswer != Question.UNANSWERED && !mLocalDataset.answer.equals(mLocalDataset.myAnswer)) {
                mAdapter.notifyItemChanged(mLocalDataset.myAnswer);
            }
            mAdapter.notifyItemChanged(mLocalDataset.answer);
        });
        mNum = view.findViewById(R.id.entity_question_num);
        mScore = view.findViewById(R.id.entity_question_score);
        mPrev = view.findViewById(R.id.entity_question_prev);
        mNext = view.findViewById(R.id.entity_question_next);
        mShare = view.findViewById(R.id.entity_question_share);
        mPrev.setOnClickListener(v -> setCurrentQuestion(currentQuestion - 1));
        mNext.setOnClickListener(v -> setCurrentQuestion(currentQuestion + 1));
        mShare.setOnClickListener(v -> {
            String wbContent;
            if (mLocalDataset.myAnswer.equals(mLocalDataset.answer)) {
                wbContent = "这道题我做对了，相信你也能做对！"; // I'm right
            } else if (mLocalDataset.myAnswer != Question.UNANSWERED) {
                wbContent = "这道题我做错了，看看你会不会？"; // I'm wrong
            } else if (mIsShowAnswer.isChecked()) {
                wbContent = "这道题我看了答案才会，看看你能不能直接做出来？";
            } else {
                wbContent = "这道题我还没做，一起来看看吧！";
            }
            share(wbContent, mLocalDataset.question, mLocalDataset.answerStr());
        });

        mView = view.findViewById(R.id.entity_question_choice_view);
        mView.setLayoutManager(new LinearLayoutManager(requireContext()));
        mAdapter = new RecyclerView.Adapter<ViewHolder>() {
            @Override
            public long getItemId(int position) {
                return mLocalDataset.choices.get(position).hashCode();
            }

            @SuppressLint("NotifyDataSetChanged")
            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(requireContext()).inflate(R.layout.card_entity_question_choice, parent, false);
                ViewHolder h = new ViewHolder(view);
                MaterialCardView c = h.getCard();
                c.setOnClickListener(v -> {
                    // correct
                    if (mLocalDataset.myAnswer == Question.UNANSWERED) {
                        mLocalDataset.myAnswer = h.getBindingAdapterPosition();
                        updateScore(); // naive
                        mAdapter.notifyDataSetChanged();
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
        mView.setItemAnimator(null);
        // ((SimpleItemAnimator) mView.getItemAnimator()).setSupportsChangeAnimations(false);
        initData();
        return view;
    }

    private void parseData(String raw_data) {
        Log.i("EntityQuestionFragment", raw_data);
        try {
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
                answerStr = answerStr.replace("答案", "");
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initData() {
        int cur = 0;
        for (; cur < mUri.length && Storage.contains(requireContext(), mStorageKey[cur]); ++cur) {
            parseData(Storage.load(requireContext(), mStorageKey[cur]));
            if (cur == 0) {
                mLoading.setVisibility(View.GONE);
                setCurrentQuestion(0);
            }
        }
        if (cur < mUri.length) {
            mHandler = new SafeHandler(this, cur);
            HttpUtils.getQuestion(mCourse[cur], mLabel[cur], mHandler);
        } else {
            loadingAccomplished = true;
            setCurrentQuestion(currentQuestion);
        }
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    private void setCurrentQuestion(int index) {
        if (index < 0 || index >= mAllDataset.size()) {
            return;
        }

        currentQuestion = index;
        mAdapter.notifyItemRangeRemoved(0, mLocalDataset.choices.size());
        mLocalDataset = mAllDataset.get(index);
        if (loadingAccomplished) {
            mNum.setText(String.format("%d/%d", index + 1, mAllDataset.size()));
        } else {
            mNum.setText(index + 1 + "/加载中");
        }
        mQuestion.setText(mLocalDataset.question);
        mAdapter.notifyItemRangeInserted(0, mLocalDataset.choices.size());
    }

    private void share(String wbContent, String title, String content) {
        if (mShareInterface != null) {
            mShareInterface.share(wbContent, title, content);
        }
    }

    @SuppressLint("DefaultLocale")
    private void updateScore() {
        double perScore = 100.0 / mAllDataset.size();
        if (mLocalDataset.myAnswer.equals(mLocalDataset.answer)) {
            score += perScore;
        }
        mScore.setText(String.format("分数: %.1f/100", score));
    }
}