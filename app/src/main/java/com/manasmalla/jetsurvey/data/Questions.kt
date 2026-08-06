package com.manasmalla.jetsurvey.data

val standardOptions = listOf("Strongly\ndisagree", "Neutral", "Strongly\nagree")

val questions = listOf(
    SurveyQuestion(
        question = "Relationship: I felt heard, understood, and respected by my therapist today.",
        options = Options.CheckboxChoice(checkboxOptions = standardOptions)
    ),
    SurveyQuestion(
        question = "Goals & Topics: We worked on and talked about what was important to me today.",
        options = Options.CheckboxChoice(checkboxOptions = standardOptions)
    ),
    SurveyQuestion(
        question = "Approach & Fit: The therapist's approach and style felt like a good fit for this session.",
        options = Options.CheckboxChoice(checkboxOptions = standardOptions)
    ),
    SurveyQuestion(
        question = "Overall Value: Overall, today's session was helpful and valuable to me.",
        options = Options.CheckboxChoice(checkboxOptions = standardOptions)
    ),
    SurveyQuestion(
        question = "Is there anything you'd like to do differently in our next session?",
        description = "Select all that apply.",
        Options.MultipleChoice(
            options = listOf(
                "More practical tools",
                "More time to talk",
                "Change pace",
                "Keep it the same"
            )
        )
    ),
//    SurveyQuestion(
//        question = "Relationship: I felt heard, understood, and respected by my therapist today.",
//        options = Options.SliderChoice(sliderOptions = listOf(
//            "Strongly\nDislike", "Neutral", "Strongly\nLike"
//        ))
//    ),
//    SurveyQuestion(
//        question = "Goals & Topics: We worked on and talked about what was important to me today.",
//        options = Options.SliderChoice(sliderOptions = listOf(
//            "Strongly\nDislike", "Neutral", "Strongly\nLike"
//        ))
//    ),
//    SurveyQuestion(
//        question = "Approach & Fit: The therapist's approach and style felt like a good fit for this session.",
//        options = Options.SliderChoice(sliderOptions = listOf(
//            "Strongly\nDislike", "Neutral", "Strongly\nLike"
//        ))
//    ),
//    SurveyQuestion(
//        question = "Overall Value: Overall, today's session was helpful and valuable to me.",
//        options = Options.SliderChoice(sliderOptions = listOf(
//            "Strongly\nDislike", "Neutral", "Strongly\nLike"
//        ))
//    ),
//    SurveyQuestion(
//        question = "Pick a Compose comic character",
//        description = "Select one.",
//        options = Options.SingleChoice(options = mapOf(
//            "Spark" to R.drawable.spark,
//            "Lenz" to R.drawable.lenz,
//            "Bug of Chaos" to R.drawable.bug_of_chaos,
//            "Frag" to R.drawable.frag
//        ))
//    ),
//    SurveyQuestion(
//        question = "When was the last time you ordered takeaway because you couldn't be bothered to cook?",
//        description = "Select date.",
//        options = Options.DateChoice
//    ),
//    SurveyQuestion(question = "Show off your selfie skills!", options = Options.ImageChoice)
)