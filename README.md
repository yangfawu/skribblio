# skribblio

This is a replica of the popular drawing game, [skribbl.io](https://skribbl.io/). The frontend is built using the Angular framework while the "backend" is built using Java. The two ends communicate with each other via listeners on a Google Firebase. This approach was used so that only a single server is needed to be hosted locally while players can play with each other via the Angular client.

### Note

The Java end requires a JSON file containing the secrets of a service account linked to an appropriate Firebase project. The project currently points to [`pogchamp-e27a7-firebase-adminsdk-n3y1a-42ae4a0df2.json`](/server/src/main/resources/service/pogchamp-e27a7-firebase-adminsdk-n3y1a-42ae4a0df2.json), but that service account has long been deactivated. In the near future, I will modify this project to have an easier set up so that it can be easily used with any Firebase project.
 