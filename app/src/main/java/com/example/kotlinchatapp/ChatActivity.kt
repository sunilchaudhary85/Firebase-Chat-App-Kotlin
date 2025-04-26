package com.example.kotlinchatapp

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinchatapp.adapter.ChatRecyclerAdapter
import com.example.kotlinchatapp.model.ChatMessageModel
import com.example.kotlinchatapp.model.ChatroomModel
import com.example.kotlinchatapp.model.UserModel
import com.example.kotlinchatapp.utils.AndroidUtil
import com.example.kotlinchatapp.utils.FirebaseUtil
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class ChatActivity : AppCompatActivity() {

    private lateinit var otherUser: UserModel
    private lateinit var chatroomId: String
    private lateinit var chatroomModel: ChatroomModel
    private lateinit var adapter: ChatRecyclerAdapter

    private lateinit var messageInput: EditText
    private lateinit var sendMessageBtn: ImageButton
    private lateinit var backBtn: ImageButton
    private lateinit var otherUsername: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // Get UserModel
        otherUser = AndroidUtil.getUserModelFromIntent(intent)
        chatroomId = FirebaseUtil.getChatroomId(FirebaseUtil.currentUserId(), otherUser.userId)

        messageInput = findViewById(R.id.chat_message_input)
        sendMessageBtn = findViewById(R.id.message_send_btn)
        backBtn = findViewById(R.id.back_btn)
        otherUsername = findViewById(R.id.other_username)
        recyclerView = findViewById(R.id.chat_recycler_view)
        imageView = findViewById(R.id.profile_pic_image_view)

        FirebaseUtil.getOtherProfilePicStorageRef(otherUser.userId).downloadUrl
                .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val uri = task.result
                AndroidUtil.setProfilePic(this, uri, imageView)
            }
        }

        backBtn.setOnClickListener {
            onBackPressed()
        }
        otherUsername.text = otherUser.username

        sendMessageBtn.setOnClickListener {
            val message = messageInput.text.toString().trim()
            if (message.isEmpty()) return@setOnClickListener
                    sendMessageToUser(message)
        }

        getOrCreateChatroomModel()
        setupChatRecyclerView()
    }

    private fun setupChatRecyclerView() {
        val query = FirebaseUtil.getChatroomMessageReference(chatroomId)
                .orderBy("timestamp", Query.Direction.DESCENDING)

        val options = FirestoreRecyclerOptions.Builder<ChatMessageModel>()
                .setQuery(query, ChatMessageModel::class.java)
            .build()

        adapter = ChatRecyclerAdapter(options, applicationContext)
        val manager = LinearLayoutManager(this)
        manager.reverseLayout = true
        recyclerView.layoutManager = manager
        recyclerView.adapter = adapter
        adapter.startListening()
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                recyclerView.smoothScrollToPosition(0)
            }
        })
    }

    private fun sendMessageToUser(message: String) {
        chatroomModel.lastMessageTimestamp = Timestamp.now()
        chatroomModel.lastMessageSenderId = FirebaseUtil.currentUserId()
        chatroomModel.lastMessage = message
        FirebaseUtil.getChatroomReference(chatroomId).set(chatroomModel)

        val chatMessageModel = ChatMessageModel(
                message,
                FirebaseUtil.currentUserId(),
                Timestamp.now()
        )

        FirebaseUtil.getChatroomMessageReference(chatroomId).add(chatMessageModel)
                .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                messageInput.text.clear()
                sendNotification(message)
            }
        }
    }

    private fun getOrCreateChatroomModel() {
        FirebaseUtil.getChatroomReference(chatroomId).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                chatroomModel = task.result?.toObject(ChatroomModel::class.java) ?: run {
                    // First time chat
                    ChatroomModel(
                            chatroomId,
                            listOf(FirebaseUtil.currentUserId(), otherUser.userId),
                            Timestamp.now(),
                            ""
                    ).also {
                        FirebaseUtil.getChatroomReference(chatroomId).set(it)
                    }
                }
            }
        }
    }

    private fun sendNotification(message: String) {
        FirebaseUtil.currentUserDetails().get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val currentUser = task.result?.toObject(UserModel::class.java)
                try {
                    val jsonObject = JSONObject().apply {
                        put("notification", JSONObject().apply {
                            put("title", currentUser?.username)
                            put("body", message)
                        })
                        put("data", JSONObject().apply {
                            put("userId", currentUser?.userId)
                        })
                        put("to", otherUser.fcmToken)
                    }
                    callApi(jsonObject)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun callApi(jsonObject: JSONObject) {
      //  val JSON = MediaType.get("application/json; charset=utf-8")
        val client = OkHttpClient()
        val url = "https://fcm.googleapis.com/fcm/send"
      //  val body = RequestBody.create(JSON, jsonObject.toString())
        val request = Request.Builder()
                .url(url)
            //    .post(body)
                .header("Authorization", "Bearer YOUR_API_KEY")
                .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                // Handle response
            }
        })
    }
}