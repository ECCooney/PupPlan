
# PupPlan

## Overview
PupPlan is an app to track dog friendly locations in Ireland, and the events happening at those locations

Within a created Location, the user can then create list of events. Each event captures information on the:

## App Functionalities
### Backend Storage
Firebase is used to manage the backend requirements of the app, including real-time database data storage for text data and Google cloud storage for images.

New images are stored according to their file name, unless they are profile pictures, in which case they are stored according to their user ID. Each time a new image is stored, a check is performed to determine if the file name already exists. If the file does not exist it is added, but if the file does exist (e.g. it was used previously) then there is no upload and the existing file path is used for displaying the image. In this way, storage duplication of images is avoided. 
 
### Create Account/Sign-in
Firebase has also been used to handle authentication of users. Users are able to initially create an account using Google authentication within Firebase, or alternatively, they can also use a valid email/password combination. 
  
### Nav Drawer/MVVM Framework and Navigation
The framework used is model–view–viewmodel (MVVM), with nav drawer menu functionality for navigating to different pages, accessing map, or signing out. 
 
For navigation of pages outside the nav drawer (e.g. Details, New Location etc) the top menu includes Home and Back buttons, where Home brings the user back to the Location List page, while Back brings the user back to the previous screen.
 
### User Profile Pictures
Users signing in with a Google account (via Google authorisation) are automatically assigned a profile picture from their user account. Users can also change that image manually by clicking on the image
 
### Creating a New Location
Users can create a new Location, including details on features such as:
-	Title
-	Description
-	Category (from spinner menu)
- Location image 
 
### Creating an Event
An event can be created from the events page accessed from the location details page. 
Events include details on features such as:
-	Title
-	Description
-	Event cost (from spinner menu)
-	Start date (from drop down using some of Androids date and time functionality)
-	Multiple images
-	Location set manually
 
### Updating or Deleting a Location
Clicking into the location details from the locations list takes the user to the Location Details view and allows the user to update details or delete the location altogether. 
 
### Using Swipe to Edit/Delete Events from List
Events can be edited (updated) and deleted by using swipe functions, with a left swipe deleting the specific event, and a right swipe taking the user to the respective event Details page. A list can also be refreshed by dragging down on the list.     

### Locations and Events Map
Google services has been utilised to generate maps for the application. The users current location is autosaved when adding a location, whereas for an event, a marker can be dragged to the appropriate spot.

### Adding/Removing Favourites
The ability of users to compile a list of favourite events. 
 
To add an event as a favourite, the user clicks on the location in the list, and within the detail view, clicks the Add To Favourites Button at the bottom of the screen. This then takes the user back to the Events List, but the event is now including a star to signal it has been favourited. To remove  a favourite, the user goes back to the event details page, scrolls to the bottom again, and clicks remove from favourites
   
### Filtering Function
Filtering functionality have been provided in both the Location and Event list views.
   
Show All or the correct type will show the corresponding locations/events. 

## Non functioning/incomplete code and functions (TO-DO):

### Favourites Map/ Listing all favourites.

Initially I aimed to use the favourites as an upcoming plans feature however this was not functioning correctly. The code has been started in some areas but is incomplete.

Similar to the other maps, the favourites map shows events that have been favourited by the user. Favourites can be clicked on to show card details.

### Locations expected behaviour

Due to the nature of the application, it would have made more sense to have the location lat and long be entered manually through drag and drop, this then autofilled for events at that location, and the current user location used only for user details or "check ins". 

## Diagrams

### Navigation Diagram

![](https://res.cloudinary.com/drnpyxlgc/image/upload/v1702824866/navigation_map_scuhpj.png)

## Sources Used
https://firebase.google.com/docs/storage/android/upload-files
https://firebase.google.com/docs/storage
https://firebase.google.com/docs/storage/security
https://github.com/bumptech/glide
https://www.geeksforgeeks.org/recyclerview-using-gridlayoutmanager-in-android-with-example/
https://stackoverflow.com/questions/50999112/date-and-time-in-android-studio-kotlin-language
   



