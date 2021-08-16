package com.example.firebasesocialapp.model

data class Notification(val userid: String = "",
                        val text: String = "",
                        val postid: String = "",
                        val isPost: Boolean = false)