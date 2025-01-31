An android app created for blue-and-great-tits-monitoring project. Within the Project, students would receive a different list of nesting boxes to monitor each day and set out to the local forrest, collecting data on nest-building-progression, eggs present and birds spotted near the nest.
The app makes this easy by providing a map, highlighting the location of each box and facilitating easy notekeeping of observations.
It also allows to upload and format the collected data into a shared excel spread sheet located within a shared dropbox folder.
While it is highly customized to its specific task, I hope it can inspire fellow junior researchers with an interest in app-development to develop custom solutions for your research questions.


<p align="center">
<img  src="https://github.com/user-attachments/assets/59e9def6-6d4d-4e17-a5e7-80d337bd2f05" alt="Centered Image" width="200" />
</p>
Main Menu: Here the boxes to check are selected prior to sampling. This Data is stored as a JSON-String. You can access the map as well as a preview of the data collected so far under results.

<p align="center">
<img  src="https://github.com/user-attachments/assets/43a89cca-513b-496c-92f3-f1dea9eed5ea" alt="Centered Image" width="200" />
</p>
Map. Since advanced navigation wasn't necessary for this project, I opted to simply use a screenshot and placed buttons to represent the nestboxes. Green boxes have beem already checked today, while red ones remain to be visited.
When u zoom / drag across the map, the button position is updated accordingly. Only active Nestboxes are visible.
For performance reason, upon selecting which nestboxes need to be check, unused buttons are freed at runtime.

<p align="center">
<img  src="https://github.com/user-attachments/assets/a7ec5e1c-3803-4eb1-9f45-e99c20369372" alt="Centered Image" width="200" />
</p>
The View upon selecting a Nestbox. Data is conveniently entered using checkboxes / Spinners only. Neststate shows the progression of the nestbuilding.
The Right side view is only visible for boxes in a state of progression where egg laying has already started.

DROPBOX-CONNECTION
If you want to use the dropbox functionality you will have to register the app with dropbox and replace the BLANKED OUT key within the Manifest.xml file.
You will also have to replace the _________ in ___________ with the path to your ?shared? dropbox location.
For a more detailed explanation visit: https://www.dropbox.com/developers/reference/getting-started
