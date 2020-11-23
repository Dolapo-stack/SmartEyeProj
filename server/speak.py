'''This module takes in Speech and uses
Natural Langu
age Processing to convert
to text and then parses text to check if it
matches a dictionary of "Panic words"
Author: Oluwaseun Akindolire 
Version: 28-07-2020
'''

import os
import random

import speech_recognition as sr


class Speak:
    dict_of_panic_words = {"help", "please", "stop", "pain", "danger", "breathe", "no", "hurt"}

    # Initialize speak class 
    def __init__(self):
        super().__init__()

    # gets word from speech
    # convert to list
    # check each token in list
    # is any of the token a panic word?
    def has_panic_terms(self, speech):
        terms_detected = []
        for term in speech.split():
            # print(term)
            if term in self.dict_of_panic_words:
                terms_detected.append(term)
                return terms_detected
        return False
    def listen_to_user(self):

        # Initialize recognizer class 
        # recognizer() for recognizing user speech from the microphone
        recognize_speech = sr.Recognizer()
        with sr.Microphone() as source:  # microphone as source
            print("Hello there..please say soomethiing...\n")
            # listening for speech and store in audio variable
            audio = recognize_speech.listen(source)
            voice_to_text = ''

            try:
                # convert audio to text
                voice_to_text = recognize_speech.recognize_google(audio)

                # recoginize_() method will throw a request error if the API is unreachable
            except sr.UnknownValueError:
                print("Sorry, I did not get that")
            except sr.RequestError:
                # error: recognizer is not connected
                print("Sorry, the service is down")

            print(f">> This is what I heard: {voice_to_text}")

            if (self.has_panic_terms(voice_to_text)):
                print(self.has_panic_terms(voice_to_text))
                print("PANIC MODE ACTIVATED!")
            else:
                print("USER IS SAFE!")

            return voice_to_text


my_speech = Speak()
my_speech.listen_to_user()
