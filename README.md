# My Hub — School & Money (Android)

An Android Jetpack Compose application rewritten from the original web app (`My Hub — School & Money`), featuring on-device Room + DataStore persistence and Material Design 3 styling with custom bundled typography (`Bricolage Grotesque`, `Sora`, and `Figtree`).

## Features

- **Private Hub Lock Screen**: Password-gated entry (`Show`/`Hide` toggle, error feedback with shake animation, and top-bar `Lock` action).
- **School Planner**:
  - **Tomorrow**: Highlighted tomorrow view, class schedule summary, expandable weekly timetable editor (Mon–Sun), and daily-resetting "Pack your bag" checklist.
  - **Homework**: To-do list sorted by completion and due date with status tags (`Overdue`, `Today`, `Tomorrow`, weekday/date) plus a 7-day "Week ahead" overview.
  - **Exams & Study**: Exam tracker with comma-separated topics, interactive topic chips, progress bar, tailored study method tips, daily topic pace calculation, study list, and Pomodoro study timer (`25:00` Focus / `05:00` Break).
- **My Money**:
  - **Log & History**: Track spent and earned transactions by category and date, view today/7-day/monthly summaries, and expand/collapse recent history.
  - **Budget & Goals**: Set monthly spending budget with daily pacing and color-coded progress bar, per-category monthly spending breakdown, and savings goal tracker.
  - **Settings**: Select currency symbol (`$`, `£`, `€`, `¥`, `₹`, `R`, `kr`) and two-tap confirmation to erase all money data.
- **Theme Support**: Toggle Light and Dark themes (plus automatic School indigo vs. Money blue accent modes).
