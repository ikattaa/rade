* Contexte :

	Pour réaliser un import complet des communes dans l'application RADE
	il est nécessaire d'utiliser deux batchs :
		- Le premier  batch "importCommuneSimpleInsee" permet l'import
		d'un fichier qui contient les communes existantes à une date donnée.
		Cela permet d'initialiser les communes en base de données RADE. 
		Pour réaliser cet import initial on utilise le fichier de 1999 "comsimp1999.txt".
			
		- Le second batch "importCommuneInseeHistory" permet l'import d'un fichier d'historique
		qui contient l'ensemble des modifications réalisées sur les communes depuis 1943. 
		On utilise le fichier d'historique le plus récent fournit par l'INSEE.
		
	Le fichier comsimp1999.txt contient des incohérences avec les fichiers d'historique
	et les fichiers annuels ultérieurs à 1999.
	Les fichiers d'historique de l'INSEE ne contiennent pas les modifications sur les DROM.

	Nous avons modifié le fichier "comsimp1999.txt" pour corriger les problèmes que nous avons détectés
	et nous avons créé un nouveau fichier d'historique "mvtcommunes-97.csv" dédié aux DROM. 

* Liste des fichiers :

*** comsimp1999_avant_PEC_2021.txt :
	Fichier INSEE des communes de 1999 partiellement corrigé. 
	55 noms de communes ont été modifiés par rapport au fichier original.
	Importer avec la date du 01/01/1999.

*** comsimp1999.txt : 
	Fichier INSEE des communes de 1999 contenant les modifications
	réalisées sur le fichier comsimp1999_avant_PEC_2021.txt ainsi que des modications complémentaires.
	Dans le fichier de communes de 2021 les noms majuscules ne contiennent pas de caratères spéciaux.
	Nous avons supprimé les caractères spéciaux des noms en majuscules de 12795 communes.
	Nous avons également détecté et corrigé des écarts sur 4 noms en minuscule au niveaux des caractères spéciaux.
	Ce fichier doit être importé avec la date du 01/01/1999.

*** mvtcommunes-97.csv :
	Fichier dédié à l'historique des communes des DROM qui commencent par 97
	et qui ne sont pas traitées dans le fichier d'historique de l'INSEE.
	Ce fichier comprent l'ajout de 19 communes (opérations code 20) 
	et la suppression de 2 communes (opération code 30).
	Ce fichier doit être importé avec la date du 01/01/1976.