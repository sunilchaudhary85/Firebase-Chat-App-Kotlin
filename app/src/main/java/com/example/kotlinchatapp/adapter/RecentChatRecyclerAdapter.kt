package com.example.kotlinchatapp.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinchatapp.ChatActivity
import com.example.kotlinchatapp.R
import com.example.kotlinchatapp.model.ChatroomModel
import com.example.kotlinchatapp.model.UserModel
import com.example.kotlinchatapp.utils.AndroidUtil
import com.example.kotlinchatapp.utils.FirebaseUtil
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class RecentChatRecyclerAdapter(
    options: FirestoreRecyclerOptions<ChatroomModel>,
    private val context: Context
) : FirestoreRecyclerAdapter<ChatroomModel, RecentChatRecyclerAdapter.ChatroomModelViewHolder>(options) {

    override fun onBindViewHolder(holder: ChatroomModelViewHolder, position: Int, model: ChatroomModel) {
        FirebaseUtil.getOtherUserFromChatroom(model.userIds)
            .get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val lastMessageSentByMe = model.lastMessageSenderId == FirebaseUtil.currentUserId()

                    val otherUserModel = task.result?.toObject(UserModel::class.java)

                    FirebaseUtil.getOtherProfilePicStorageRef(otherUserModel?.userId ?: "")
                        .getDownloadUrl()
                        .addOnCompleteListener { t ->
                            if (t.isSuccessful) {
                                val uri: Uri = t.result ?: return@addOnCompleteListener
                                AndroidUtil.setProfilePic(context, uri, holder.profilePic)
                            }
                        }

                    holder.usernameText.text = otherUserModel?.username
                    if (lastMessageSentByMe) {
                        holder.lastMessageText.text = "You: ${model.lastMessage}"
                    } else {
                        holder.lastMessageText.text = model.lastMessage
                    }
                    holder.lastMessageTime.text = FirebaseUtil.timestampToString(model.lastMessageTimestamp)

                    holder.itemView.setOnClickListener {
                        // Navigate to chat activity
                        val intent = Intent(context, ChatActivity::class.java)
                        AndroidUtil.passUserModelAsIntent(intent, otherUserModel!!)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        context.startActivity(intent)
                    }
                }
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatroomModelViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.recent_chat_recycler_row, parent, false)
        return ChatroomModelViewHolder(view)
    }

    inner class ChatroomModelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val usernameText: TextView = itemView.findViewById(R.id.user_name_text)
        val lastMessageText: TextView = itemView.findViewById(R.id.last_message_text)
        val lastMessageTime: TextView = itemView.findViewById(R.id.last_message_time_text)
        val profilePic: ImageView = itemView.findViewById(R.id.profile_pic_image_view)
    }
}
