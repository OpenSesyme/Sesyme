package com.sesyme.sesyme.Fragments;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.sesyme.sesyme.DashboardActivity;
import com.sesyme.sesyme.data.Methods;
import com.sesyme.sesyme.data.SefnetContract;
import com.sesyme.sesyme.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;

import javax.annotation.Nullable;

/**
 * A simple {@link Fragment} subclass.
 */
@SuppressWarnings("ConstantConditions")
public class InterestFragment extends Fragment {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String sBusiness, sEconomics, sChemistry, sPhysics, sMathematics, sArts, sAgriculture, sGeo, sReligion,
            sLaw, sHealth, sEngineering, sComputer, sEducation, sHuman, sAccounting, sMultimedia, sBiology;
    private ImageView agricultureThumb, geoThumb, religiousThumb;
    private ImageView fictionThumb;
    private ImageView humanThumb;
    private ImageView economicsThumb;
    private ImageView thrillerThumb;
    private ImageView physicsThumb;
    private ImageView mathsThumb;
    private ImageView healthThumb;
    private ImageView engineeringThumb;
    private ImageView artsThumb;
    private ImageView businessThumb;
    private ImageView chemistryThumb;
    private ImageView educationThumb;
    private ImageView accountingThumb;
    private ImageView multimediaThumb;
    private ImageView biologyThumb;
    private String email;
    private Button save;
    private ArrayList<String> interests;
    private CollectionReference usersRef;
    private Methods methods;

    public InterestFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View interestsView = inflater.inflate(R.layout.activity_interests, container, false);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            email = user.getEmail();
        }

        interests = new ArrayList<>();
        methods = new Methods(getActivity());

        ImageView computerImg = interestsView.findViewById(R.id.computer_img_interests);
        fictionThumb = interestsView.findViewById(R.id.fiction_thumb_interests);
        ImageView humanImg = interestsView.findViewById(R.id.self_help_img_interests);
        humanThumb = interestsView.findViewById(R.id.self_help_thumb_interests);
        ImageView economicsImg = interestsView.findViewById(R.id.life_style_img_interests);
        economicsThumb = interestsView.findViewById(R.id.life_style_thumb_interests);
        ImageView lawImg = interestsView.findViewById(R.id.law_img_interests);
        thrillerThumb = interestsView.findViewById(R.id.thriller_thumb_interests);
        ImageView physicsImg = interestsView.findViewById(R.id.non_fiction_img_interests);
        physicsThumb = interestsView.findViewById(R.id.non_fiction_thumb_interests);
        ImageView mathsImg = interestsView.findViewById(R.id.kids_img_interests);
        mathsThumb = interestsView.findViewById(R.id.kids_thumb_interests);
        ImageView romanceImg = interestsView.findViewById(R.id.romance_img_interests);
        healthThumb = interestsView.findViewById(R.id.romance_thumb_interests);
        ImageView classicsImg = interestsView.findViewById(R.id.classics_img_interests);
        engineeringThumb = interestsView.findViewById(R.id.classics_thumb_interests);
        ImageView poetryImg = interestsView.findViewById(R.id.poetry_img_interests);
        artsThumb = interestsView.findViewById(R.id.poetry_thumb_interests);
        ImageView businessImg = interestsView.findViewById(R.id.business_img_interests);
        businessThumb = interestsView.findViewById(R.id.business_thumb_interests);
        ImageView sciFiImg = interestsView.findViewById(R.id.sci_fi_img_interests);
        chemistryThumb = interestsView.findViewById(R.id.sci_fi_thumb_interests);
        ImageView biographyImg = interestsView.findViewById(R.id.biographies_img_interests);
        educationThumb = interestsView.findViewById(R.id.biographies_thumb_interests);
        ImageView accountingImg = interestsView.findViewById(R.id.accounting_img_interests);
        accountingThumb = interestsView.findViewById(R.id.accounting_thumb_interests);
        ImageView multimediaImg = interestsView.findViewById(R.id.multimedia_img_interests);
        multimediaThumb = interestsView.findViewById(R.id.multimedia_thumb_interests);
        ImageView biologyImg = interestsView.findViewById(R.id.biology_img_interests);
        biologyThumb = interestsView.findViewById(R.id.biology_thumb_interests);
        ImageView agricultureImg = interestsView.findViewById(R.id.agriculture_img_interests);
        agricultureThumb = interestsView.findViewById(R.id.agriculture_thumb_interests);
        ImageView religionImg = interestsView.findViewById(R.id.religious_img_interests);
        religiousThumb = interestsView.findViewById(R.id.religious_thumb_interests);
        ImageView geoImg = interestsView.findViewById(R.id.geography_img_interests);
        geoThumb = interestsView.findViewById(R.id.geography_thumb_interests);

        TextView tArts = interestsView.findViewById(R.id.arts_text_interests);
        TextView tBusiness = interestsView.findViewById(R.id.business_text_interests);
        TextView tChemistry = interestsView.findViewById(R.id.chemistry_text_interests);
        TextView tComputer = interestsView.findViewById(R.id.computer_text_interests);
        TextView tEconomics = interestsView.findViewById(R.id.economics_text_interests);
        TextView tEducation = interestsView.findViewById(R.id.education_text_interests);
        TextView tEngineering = interestsView.findViewById(R.id.engineering_text_interests);
        TextView tHealth = interestsView.findViewById(R.id.health_text_interests);
        TextView tLaw = interestsView.findViewById(R.id.law_text_interests);
        TextView tMathematics = interestsView.findViewById(R.id.mathematics_text_interests);
        TextView tPhysics = interestsView.findViewById(R.id.physics_text_interests);
        TextView tHuman = interestsView.findViewById(R.id.human_science_text_interests);
        TextView tAccounting = interestsView.findViewById(R.id.accounting_text_interests);
        TextView tMultimedia = interestsView.findViewById(R.id.multimedia_text_interests);
        TextView tBiology = interestsView.findViewById(R.id.biology_text_interests);
        TextView tAgriculture = interestsView.findViewById(R.id.agriculture_text_interests);
        TextView tReligion = interestsView.findViewById(R.id.religious_text_interests);
        TextView tGeo = interestsView.findViewById(R.id.geography_text_interests);

        sBusiness = tBusiness.getText().toString();
        sEconomics = tEconomics.getText().toString();
        sChemistry = tChemistry.getText().toString();
        sPhysics = tPhysics.getText().toString();
        sMathematics = tMathematics.getText().toString();
        sArts = tArts.getText().toString();
        sLaw = tLaw.getText().toString();
        sHealth = tHealth.getText().toString();
        sEngineering = tEngineering.getText().toString();
        sComputer = tComputer.getText().toString();
        sEducation = tEducation.getText().toString();
        sHuman = tHuman.getText().toString();
        sAccounting = tAccounting.getText().toString();
        sMultimedia = tMultimedia.getText().toString();
        sBiology = tBiology.getText().toString();
        sAgriculture = tAgriculture.getText().toString();
        sReligion = tReligion.getText().toString();
        sGeo = tGeo.getText().toString();

        usersRef = db.collection(SefnetContract.USER_DETAILS);

        save = interestsView.findViewById(R.id.fab_next_interests);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String titleString = (String) getActivity().getTitle();
                if (titleString.equals("Choose Interests")) {
                    usersRef.document(email).update("interests", interests);
                    startActivity(new Intent(getActivity(), DashboardActivity.class));
                } else {
                    usersRef.document(email).update("interests", interests);
                    getActivity().finish();
                }
            }
        });

        if (email != null) {
            DocumentReference userRef = usersRef.document(email);
            userRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                    if (e == null) {
                        if (snapshot.get("interests") != null) {
                            //noinspection unchecked
                            interests = (ArrayList<String>) snapshot.get("interests");
                        }

                        if (interests != null && interests.size() < 2) {
                            save.setVisibility(View.GONE);
                        } else {
                            save.setVisibility(View.VISIBLE);
                        }
                        if (interests != null) {
                            if (interests.contains(sLaw)) {
                                fictionThumb.setVisibility(View.VISIBLE);
                            } else {
                                fictionThumb.setVisibility(View.GONE);
                            }
                            if (interests.contains(sHuman)) {
                                humanThumb.setVisibility(View.VISIBLE);
                            } else {
                                humanThumb.setVisibility(View.GONE);
                            }
                            if (interests.contains(sEconomics)) {
                                economicsThumb.setVisibility(View.VISIBLE);
                            } else {
                                economicsThumb.setVisibility(View.GONE);
                            }
                            if (interests.contains(sComputer)) {
                                thrillerThumb.setVisibility(View.VISIBLE);
                            } else {
                                thrillerThumb.setVisibility(View.GONE);
                            }
                            if (interests.contains(sPhysics)) {
                                physicsThumb.setVisibility(View.VISIBLE);
                            } else {
                                physicsThumb.setVisibility(View.GONE);
                            }
                            if (interests.contains(sMathematics)) {
                                mathsThumb.setVisibility(View.VISIBLE);
                            } else {
                                mathsThumb.setVisibility(View.GONE);
                            }
                            if (interests.contains(sHealth)) {
                                healthThumb.setVisibility(View.VISIBLE);
                            } else {
                                healthThumb.setVisibility(View.GONE);
                            }
                            if (interests.contains(sEngineering)) {
                                engineeringThumb.setVisibility(View.VISIBLE);
                            } else {
                                engineeringThumb.setVisibility(View.GONE);
                            }
                            if (interests.contains(sArts)) {
                                artsThumb.setVisibility(View.VISIBLE);
                            } else {
                                artsThumb.setVisibility(View.GONE);
                            }
                            if (interests.contains(sEducation)) {
                                educationThumb.setVisibility(View.VISIBLE);
                            } else {
                                educationThumb.setVisibility(View.GONE);
                            }
                            if (interests.contains(sChemistry)) {
                                chemistryThumb.setVisibility(View.VISIBLE);
                            } else {
                                chemistryThumb.setVisibility(View.GONE);
                            }
                            if (interests.contains(sBusiness)) {
                                businessThumb.setVisibility(View.VISIBLE);
                            } else {
                                businessThumb.setVisibility(View.GONE);
                            }
                            if (interests.contains(sAccounting)) {
                                accountingThumb.setVisibility(View.VISIBLE);
                            } else {
                                accountingThumb.setVisibility(View.GONE);
                            }
                            if (interests.contains(sMultimedia)) {
                                multimediaThumb.setVisibility(View.VISIBLE);
                            } else {
                                multimediaThumb.setVisibility(View.GONE);
                            }
                            if (interests.contains(sBiology)) {
                                biologyThumb.setVisibility(View.VISIBLE);
                            } else {
                                biologyThumb.setVisibility(View.GONE);
                            }
                            if (interests.contains(sAgriculture)) {
                                agricultureThumb.setVisibility(View.VISIBLE);
                            } else {
                                agricultureThumb.setVisibility(View.GONE);
                            }
                            if (interests.contains(sReligion)) {
                                religiousThumb.setVisibility(View.VISIBLE);
                            } else {
                                religiousThumb.setVisibility(View.GONE);
                            }
                            if (interests.contains(sGeo)) {
                                geoThumb.setVisibility(View.VISIBLE);
                            } else {
                                geoThumb.setVisibility(View.GONE);
                            }
                        }
                    }
                }
            });
        }

        computerImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                methods.LocateStringInterests(interests, sComputer, fictionThumb);
                if (interests.size() > 2) {
                    save.setVisibility(View.VISIBLE);
                } else {
                    save.setVisibility(View.GONE);
                }
            }
        });

        humanImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                methods.LocateStringInterests(interests, sHuman, humanThumb);
                if (interests.size() > 2) {
                    save.setVisibility(View.VISIBLE);
                } else {
                    save.setVisibility(View.GONE);
                }
            }
        });

        economicsImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                methods.LocateStringInterests(interests, sEconomics, economicsThumb);
                if (interests.size() > 2) {
                    save.setVisibility(View.VISIBLE);
                } else {
                    save.setVisibility(View.GONE);
                }
            }
        });

        lawImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                methods.LocateStringInterests(interests, sLaw, thrillerThumb);
                if (interests.size() > 2) {
                    save.setVisibility(View.VISIBLE);
                } else {
                    save.setVisibility(View.GONE);
                }
            }
        });

        physicsImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                methods.LocateStringInterests(interests, sPhysics, physicsThumb);
                if (interests.size() > 2) {
                    save.setVisibility(View.VISIBLE);
                } else {
                    save.setVisibility(View.GONE);
                }
            }
        });

        mathsImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                methods.LocateStringInterests(interests, sMathematics, mathsThumb);
                if (interests.size() > 2) {
                    save.setVisibility(View.VISIBLE);
                } else {
                    save.setVisibility(View.GONE);
                }
            }
        });

        romanceImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                methods.LocateStringInterests(interests, sHealth, healthThumb);
                if (interests.size() > 2) {
                    save.setVisibility(View.VISIBLE);
                } else {
                    save.setVisibility(View.GONE);
                }
            }
        });

        classicsImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                methods.LocateStringInterests(interests, sEngineering, engineeringThumb);
                if (interests.size() > 2) {
                    save.setVisibility(View.VISIBLE);
                } else {
                    save.setVisibility(View.GONE);
                }
            }
        });

        poetryImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                methods.LocateStringInterests(interests, sArts, artsThumb);
                if (interests.size() > 2) {
                    save.setVisibility(View.VISIBLE);
                } else {
                    save.setVisibility(View.GONE);
                }
            }
        });

        businessImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                methods.LocateStringInterests(interests, sBusiness, businessThumb);
                if (interests.size() > 2) {
                    save.setVisibility(View.VISIBLE);
                } else {
                    save.setVisibility(View.GONE);
                }
            }
        });

        sciFiImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                methods.LocateStringInterests(interests, sChemistry, chemistryThumb);
                if (interests.size() > 2) {
                    save.setVisibility(View.VISIBLE);
                } else {
                    save.setVisibility(View.GONE);
                }
            }
        });

        biographyImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                methods.LocateStringInterests(interests, sEducation, educationThumb);
                if (interests.size() > 2) {
                    save.setVisibility(View.VISIBLE);
                } else {
                    save.setVisibility(View.GONE);
                }
            }
        });

        accountingImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                methods.LocateStringInterests(interests, sAccounting, accountingThumb);
                if (interests.size() > 2) {
                    save.setVisibility(View.VISIBLE);
                } else {
                    save.setVisibility(View.GONE);
                }
            }
        });

        multimediaImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                methods.LocateStringInterests(interests, sMultimedia, multimediaThumb);
                if (interests.size() > 2) {
                    save.setVisibility(View.VISIBLE);
                } else {
                    save.setVisibility(View.GONE);
                }
            }
        });

        biologyImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                methods.LocateStringInterests(interests, sBiology, biologyThumb);
                if (interests.size() > 2) {
                    save.setVisibility(View.VISIBLE);
                } else {
                    save.setVisibility(View.GONE);
                }
            }
        });

        agricultureImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                methods.LocateStringInterests(interests, sAgriculture, agricultureThumb);
                if (interests.size() > 2) {
                    save.setVisibility(View.VISIBLE);
                } else {
                    save.setVisibility(View.GONE);
                }
            }
        });

        religionImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                methods.LocateStringInterests(interests, sReligion, religiousThumb);
                if (interests.size() > 2) {
                    save.setVisibility(View.VISIBLE);
                } else {
                    save.setVisibility(View.GONE);
                }
            }
        });

        geoImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                methods.LocateStringInterests(interests, sGeo, geoThumb);
                if (interests.size() > 2) {
                    save.setVisibility(View.VISIBLE);
                } else {
                    save.setVisibility(View.GONE);
                }
            }
        });
        return interestsView;
    }
}
