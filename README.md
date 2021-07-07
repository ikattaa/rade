# Rade - Referentiel Administrative

Le référentiel administratif a pour vocation de gérer les données attributaires sur :
* des données géographiques issues de l’INSEE : commune, département, région, pays,
* des données géographiques sur le bassin : circonscription de bassin

Ces informations sont ensuite accessibles par les autres applications du SI ainsi que par les utilisateurs.

Ne seront pas pris en compte les cantons, pseudo-canton, arrondissements et pays (utilisés dans Tiers pour les adresses). De même la cohérence avec les couches SIG fournies par l’IGN (des communes, départements, régions) n’est pas traitée ici. 

# Build

Le projet est compilé et packagé avec Apache Maven 3. Il suffit d'executer la commande suivante:
```sh
$ mvn install
```

NB: le projet requiert les drivers JDBC Oracle pour le build. Si votre repository ne le fourni pas (comme c'est le cas des repository publique par défaut de Maven) vous pouvez l'installer en local manuellement de la façon suivante:
* Télécharger le JAR "ojdbc6.jar" sur le site de Oracle: https://www.oracle.com/technetwork/apps-tech/jdbc-112010-090769.html
* Installer localement:
```sh
$ mvn install:install-file -Dfile="/path/to/ojdbc6.jar" -DgroupId="com.oracle" -DartifactId="ojdbc6" -Dversion="11.2.0.4.0" -Dpackaging="jar"
```

# Run

Le projet est conçu autour des librairies Spring, en particulier Spring-Boot pour l'application web et Spring-Batch pour les batchs.

Pour lancer l'application web:
```sh
$ java -jar rade-webapp/target/rade-webapp.war
```
Pour lancer l'application web avec des fichiers de configurations spécifiques
```sh
$ java -D"config.file.xml=file:D:/Work/rade/application-dev-context.xml" -D"logging.config=D:/Work/rade/logback-debug.xml" -jar rade-webapp/target/rade-webapp.war
```

Pour lancer un batch en ligne de commande:
```sh
$ java -jar rade-batchrunner/target/rade-batchrunner.jar -h
```

# Fichiers INSEE

Nous avons corrigé les fichiers INSEE pour pouvoir réaliser un import complet à partir des données de 1999 sans erreur.
Les fichiers modifiés sont présents ici :
https://github.com/https-github-com-RadeTeam/rade/tree/master/rade-batch/src/main/resources/batchfiles

Ces fichiers sont décrits dans le fichier suivant :
https://github.com/https-github-com-RadeTeam/rade/blob/master/rade-batch/src/main/resources/batchfiles/README.txt

La procédure d'import est décrite ici :
TODO
