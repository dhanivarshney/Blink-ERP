# Professional UI/UX Refactor for SmartRoll

This plan outlines the steps to modernize the SmartRoll app, moving from a multi-activity structure to a professional single-activity architecture with a Bottom Navigation Bar, enhanced UI components, and new analytics features.

## Proposed Changes

### [Component] Core Infrastructure & Assets

#### [MODIFY] [build.gradle](file:///C:/Users/hp/smart_attend_web/android_app/app/build.gradle)
Add dependencies for Jetpack Navigation and Lottie animations.

#### [NEW] [Nav Graph](file:///C:/Users/hp/smart_attend_web/android_app/app/src/main/res/navigation/nav_graph.xml)
Define the navigation flow between Home, Analytics, Notes, PYQ, and Profile.

#### [NEW] [Navigation Menu](file:///C:/Users/hp/smart_attend_web/android_app/app/src/main/res/menu/bottom_nav_menu.xml)
Define menu items for the BottomNavigationView.

---

### [Component] UI/UX & Navigation

#### [NEW] [SplashActivity](file:///C:/Users/hp/smart_attend_web/android_app/app/src/main/java/com/smartroll/SplashActivity.kt)
Implement a professional splash screen with logo animation.

#### [NEW] [DashboardActivity](file:///C:/Users/hp/smart_attend_web/android_app/app/src/main/java/com/smartroll/DashboardActivity.kt)
The main container for the app's fragments. Handles the Bottom Navigation Bar.

#### [MODIFY] [MainActivity](file:///C:/Users/hp/smart_attend_web/android_app/app/src/main/java/com/smartroll/MainActivity.kt)
Clean up role selection and redirect to Dashboard if already logged in.

---

### [Component] Feature Fragments

#### [NEW] [HomeFragment](file:///C:/Users/hp/smart_attend_web/android_app/app/src/main/java/com/smartroll/fragments/HomeFragment.kt)
Consolidated logic from Teacher/Student activities for attendance tracking.

#### [NEW] [AnalyticsFragment](file:///C:/Users/hp/smart_attend_web/android_app/app/src/main/java/com/smartroll/fragments/AnalyticsFragment.kt)
Summary of attendance data, including "At Risk" student detection.

#### [NEW] [NotesFragment](file:///C:/Users/hp/smart_attend_web/android_app/app/src/main/java/com/smartroll/fragments/NotesFragment.kt)
Improved interface for viewing and uploading class notes.

#### [NEW] [PyqFragment](file:///C:/Users/hp/smart_attend_web/android_app/app/src/main/java/com/smartroll/fragments/PyqFragment.kt)
Modernized PYQ list with better card designs.

#### [NEW] [ProfileFragment](file:///C:/Users/hp/smart_attend_web/android_app/app/src/main/java/com/smartroll/fragments/ProfileFragment.kt)
User profile details and logout option.

---

### [Component] Styling & Polish

#### [MODIFY] [themes.xml](file:///C:/Users/hp/smart_attend_web/android_app/app/src/main/res/values/themes.xml)
Refine colors and styles for a more professional "Glassmorphism" look.

#### [NEW] [Drawables](file:///C:/Users/hp/smart_attend_web/android_app/app/src/main/res/drawable/)
Add vector icons for navigation items (home, analytics, notes, papers, profile).

## Verification Plan

### Automated Tests
- Build the project to ensure all fragment migrations are correct.
- Verify `nav_graph.xml` links correctly to IDs.

### Manual Verification
- Launch the app and observe the Splash animation.
- Test Bottom Navigation switching between all fragments.
- Verify attendance logic (BLE) still works within the HomeFragment.
- Check the "At Risk" logic in Analytics.
