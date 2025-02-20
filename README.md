5 min Video file explaining the project -> https://drive.google.com/file/d/1G7Ap977bM8aj_Q_84AniLOF-pPGeXX2t/view?usp=sharing



🎵 Audio Transcriber App with AssemblyAI & Google Gemini Integration

This Android app allows users to transcribe audio files using the AssemblyAI API and view key points and ToDo items. The app is built with Java and uses Android Studio for development.
📁 Features
🎧 Pick Audio File from Gallery
📝 Transcribe Audio using AssemblyAI API
✅ Save & View Key Points
📋 Saved ToDo List
🔗 Integration with Google Gemini API
🚀 Getting Started
1️⃣ Clone the Repository

git clone https://github.com/your-username/your-repo-name.git
cd your-repo-name

2️⃣ Open in Android Studio
Launch Android Studio.
Click on "Open an existing project".
Select the cloned repository folder.
Let Gradle sync the project.

🔑 API Integration
AssemblyAI API
Sign up at AssemblyAI and get your API key.
Replace the API key in MainActivity.java:
private static final String API_KEY = "YOUR_ASSEMBLYAI_API_KEY";

📱 App Flow
Home Screen:
Tap "Choose audio file from gallery" to pick an audio file.
The app transcribes the audio and displays the result.

Extract Text:
Click "Extract" to send the transcribed text to the Google Gemini API.

Saved Key Points:
Click "Saved Key Points" to view stored key points.

Saved ToDo:
Click "Saved ToDo" to check saved To-Do items.


📂 How It Works

File Picker → Select an audio file.
AssemblyAI API → Uploads and transcribes the audio.
Transcription Display → Shows the result on screen.
Google Gemini API → Extracts key points (via Extract button).
Saved Key Points & ToDo → Navigate using buttons.

🛡️ Permissions
Add these permissions in AndroidManifest.xml:
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
<uses-permission android:name="android.permission.INTERNET"/>

⚡ Tech Stack
Java 
Android Studio 
AssemblyAI API 
Google Gemini API 
