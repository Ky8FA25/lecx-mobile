package com.example.lecx_mobile.utils;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Service để gọi Gemini API và tạo các đáp án sai cho quiz
 */
public class GeminiUtils {
    private static final String TAG = "GeminiService";
    // Sử dụng v1beta và model mới nhất
    // Thử các model theo thứ tự: gemini-2.0-flash, gemini-1.5-flash, gemini-1.5-pro
    private static final String[] GEMINI_MODELS = {
        "gemini-2.0-flash",
        "gemini-1.5-flash",
        "gemini-1.5-pro", 
        "gemini-pro"
    };
    private static final String GEMINI_API_BASE_V1BETA = "https://generativelanguage.googleapis.com/v1beta/models/";
    private static final String GEMINI_API_BASE_V1 = "https://generativelanguage.googleapis.com/v1/models/";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    
    private final OkHttpClient client;
    private final Gson gson;
    
    public GeminiUtils() {
        this.client = new OkHttpClient();
        this.gson = new Gson();
    }
    
    /**
     * Gọi Gemini API để tạo 3 đáp án sai dựa trên câu hỏi và đáp án đúng
     * 
     * @param question Câu hỏi
     * @param correctAnswer Đáp án đúng
     * @return CompletableFuture chứa danh sách 3 đáp án sai
     */
    public CompletableFuture<List<String>> generateWrongAnswers(String question, String correctAnswer) {
        CompletableFuture<List<String>> future = new CompletableFuture<>();
        
        // Lấy API key từ Constants (hoặc BuildConfig)
        String apiKey = Constants.GEMINI_API_KEY;
        
        if (apiKey == null || apiKey.isEmpty()) {
            Log.w(TAG, "Gemini API key is not set. Using mock answers.");
            // Trả về đáp án mock nếu không có API key
            future.complete(generateMockWrongAnswers(correctAnswer));
            return future;
        }
        
        // Tạo prompt cho Gemini - yêu cầu đáp án nhiễu cùng ngôn ngữ với đáp án đúng
        String prompt = String.format(
            "Given the question '%s' and correct answer '%s', generate 3 short incorrect but plausible answers for a multiple-choice quiz. " +
            "IMPORTANT: The wrong answers MUST be in the SAME LANGUAGE as the correct answer. " +
            "If the correct answer is in Vietnamese, all wrong answers must also be in Vietnamese. " +
            "If the correct answer is in English, all wrong answers must also be in English. " +
            "Return only the 3 answers, each on a separate line, without numbering, bullets, or letter prefixes (A, B, C, D).",
            question, correctAnswer
        );
        
        // Tạo request body
        JsonObject requestBody = new JsonObject();
        JsonArray contents = new JsonArray();
        JsonObject content = new JsonObject();
        JsonArray parts = new JsonArray();
        JsonObject part = new JsonObject();
        part.addProperty("text", prompt);
        parts.add(part);
        content.add("parts", parts);
        contents.add(content);
        requestBody.add("contents", contents);
        
        String jsonBody = gson.toJson(requestBody);
        RequestBody body = RequestBody.create(jsonBody, JSON);
        
        // Thử gọi API với model đầu tiên (dùng v1beta cho gemini-2.0-flash)
        callGeminiWithModel(apiKey, body, GEMINI_MODELS[0], 0, true, future, correctAnswer);
        
        return future;
    }
    
    /**
     * Gọi Gemini API với model cụ thể, có fallback nếu lỗi
     */
    private void callGeminiWithModel(String apiKey, RequestBody body, String model, int modelIndex, 
                                     boolean useV1Beta, CompletableFuture<List<String>> future, String correctAnswer) {
        // Sử dụng v1beta cho gemini-2.0-flash, v1 cho các model khác
        String apiBase = useV1Beta ? GEMINI_API_BASE_V1BETA : GEMINI_API_BASE_V1;
        String apiUrl = apiBase + model + ":generateContent";
        
        // Tạo request với API key trong header (theo đúng format của Gemini API)
        Request request = new Request.Builder()
                .url(apiUrl)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("X-goog-api-key", apiKey)
                .build();
        
        // Gọi API
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Error calling Gemini API with model " + model + " (v1beta=" + useV1Beta + ")", e);
                // Thử v1 nếu đang dùng v1beta, hoặc thử model tiếp theo
                if (useV1Beta && modelIndex == 0) {
                    // Thử v1 cho gemini-2.0-flash
                    Log.d(TAG, "Trying v1 for model " + model);
                    callGeminiWithModel(apiKey, body, model, modelIndex, false, future, correctAnswer);
                } else if (modelIndex < GEMINI_MODELS.length - 1) {
                    // Thử model tiếp theo với v1
                    Log.d(TAG, "Trying next model: " + GEMINI_MODELS[modelIndex + 1]);
                    callGeminiWithModel(apiKey, body, GEMINI_MODELS[modelIndex + 1], modelIndex + 1, false, future, correctAnswer);
                } else {
                    // Hết model để thử, dùng mock answers
                    future.complete(generateMockWrongAnswers(correctAnswer));
                }
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "No error body";
                    Log.e(TAG, "Gemini API error with model " + model + ": " + response.code() + " - " + response.message());
                    Log.e(TAG, "Error body: " + errorBody);
                    
                    // Nếu lỗi 404, thử v1 nếu đang dùng v1beta, hoặc thử model tiếp theo
                    if (response.code() == 404) {
                        if (useV1Beta && modelIndex == 0) {
                            // Thử v1 cho gemini-2.0-flash
                            Log.d(TAG, "Model " + model + " not found in v1beta, trying v1");
                            callGeminiWithModel(apiKey, body, model, modelIndex, false, future, correctAnswer);
                            return;
                        } else if (modelIndex < GEMINI_MODELS.length - 1) {
                            // Thử model tiếp theo
                            Log.d(TAG, "Model " + model + " not found, trying next model: " + GEMINI_MODELS[modelIndex + 1]);
                            callGeminiWithModel(apiKey, body, GEMINI_MODELS[modelIndex + 1], modelIndex + 1, false, future, correctAnswer);
                            return;
                        }
                    }
                    
                    // Nếu không phải 404 hoặc hết model, dùng mock answers
                    future.complete(generateMockWrongAnswers(correctAnswer));
                    return;
                }
                
                try {
                    String responseBody = response.body().string();
                    Log.d(TAG, "Gemini API response: " + responseBody);
                    
                    // Parse response từ Gemini
                    JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
                    JsonArray candidates = jsonResponse.getAsJsonArray("candidates");
                    
                    if (candidates != null && candidates.size() > 0) {
                        JsonObject candidate = candidates.get(0).getAsJsonObject();
                        JsonObject content = candidate.getAsJsonObject("content");
                        JsonArray parts = content.getAsJsonArray("parts");
                        
                        if (parts != null && parts.size() > 0) {
                            String text = parts.get(0).getAsJsonObject().get("text").getAsString();
                            Log.d(TAG, "Raw Gemini response text: " + text);
                            List<String> wrongAnswers = parseAnswersFromText(text);
                            Log.d(TAG, "Parsed wrong answers: " + wrongAnswers);
                            
                            // Đảm bảo có đủ 3 đáp án
                            while (wrongAnswers.size() < 3) {
                                wrongAnswers.addAll(generateMockWrongAnswers(correctAnswer));
                            }
                            
                            future.complete(wrongAnswers.subList(0, Math.min(3, wrongAnswers.size())));
                            return;
                        }
                    }
                    
                    // Nếu không parse được, dùng mock answers
                    future.complete(generateMockWrongAnswers(correctAnswer));
                    
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing Gemini response", e);
                    future.complete(generateMockWrongAnswers(correctAnswer));
                }
            }
        });
    }
    
    /**
     * Parse text từ Gemini thành danh sách đáp án
     */
    private List<String> parseAnswersFromText(String text) {
        List<String> answers = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return answers;
        }
        
        // Tách theo dòng
        String[] lines = text.split("\n");
        
        for (String line : lines) {
            String originalLine = line;
            line = line.trim();
            
            // Bỏ qua dòng trống
            if (line.isEmpty()) {
                continue;
            }
            
            // Loại bỏ số thứ tự (ví dụ: "1. ", "1) ", "1 ")
            line = line.replaceAll("^[0-9]+\\.?\\s+", "");
            
            // Loại bỏ bullet points (ví dụ: "- ", "• ", "* ")
            line = line.replaceAll("^[-•*]\\s+", "");
            
            // Loại bỏ chữ cái A-D chỉ khi có dấu chấm, dấu ngoặc đơn, hoặc khoảng trắng sau
            // Ví dụ: "A. ", "A ", "B) ", "C. "
            // KHÔNG loại bỏ nếu chữ cái là phần của từ (ví dụ: "Apple", "Banana", "Cherry")
            line = line.replaceAll("^[A-D][\\.)]\\s+", "");
            // Chỉ loại bỏ "A " nếu có khoảng trắng và không phải là từ đầy đủ
            if (line.matches("^[A-D]\\s+.*")) {
                line = line.replaceFirst("^[A-D]\\s+", "");
            }
            
            // Bỏ qua nếu sau khi parse vẫn còn quá ngắn hoặc chỉ là ký tự đơn
            if (!line.isEmpty() && line.length() > 0) {
                answers.add(line);
                Log.d(TAG, "Parsed line: '" + originalLine + "' -> '" + line + "'");
            }
        }
        
        return answers;
    }
    
    /**
     * Tạo đáp án mock nếu không có API key hoặc API lỗi
     */
    private List<String> generateMockWrongAnswers(String correctAnswer) {
        List<String> mockAnswers = new ArrayList<>();
        mockAnswers.add("Không phải " + correctAnswer);
        mockAnswers.add("Câu trả lời khác");
        mockAnswers.add("Đáp án khác");
        return mockAnswers;
    }
}

