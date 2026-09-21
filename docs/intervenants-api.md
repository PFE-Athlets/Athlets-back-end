# Gestion des intervenants

Les routes ci-dessous utilisent la session `JSESSIONID` existante et sont réservées aux administrateurs actifs. Les réponses ne contiennent ni mot de passe ni lien d'activation.

| Méthode | Route | Réponse |
|---|---|---|
| GET | `/api/intervenants` | 200, liste des administrateurs, coachs et kinésiologues |
| GET | `/api/intervenants/{id}` | 200, détails et `teamIds` |
| POST | `/api/intervenants` | 201, compte créé en attente et invitation envoyée |
| PUT | `/api/intervenants/{id}` | 200, coordonnées mises à jour |
| PUT | `/api/intervenants/{id}/deactivate` | 204 |
| PUT | `/api/intervenants/{id}/reactivate` | 200, compte mis à jour |
| POST | `/api/intervenants/{id}/resend-activation` | 204, nouvelle invitation |

Création :

```json
{
  "firstName": "Alice",
  "lastName": "Martin",
  "email": "alice@example.com",
  "phone": null,
  "username": "alice.martin",
  "role": "COACH"
}
```

Rôles acceptés : `ADMIN`, `COACH`, `KINE`. Pour modifier les coordonnées, envoyer les cinq champs de coordonnées, sans `role`. Le rôle ne se modifie pas. Le prénom, le nom et le nom d'utilisateur sont limités à 50 caractères, le courriel à 254 et le téléphone facultatif à 20. Courriel et nom d'utilisateur sont uniques dans la table commune aux utilisateurs, athlètes compris. Les comparaisons conservent la sensibilité à la casse du projet existant.

Le serveur fournit `id`, `accountStatus`, `accountCreationDate`, `accessLevel`, `role`, `accountActivated` et `teamIds`, en plus des coordonnées. Les niveaux existants sont conservés : administrateur 1, coach 2, kinésiologue 4.

## Activation et statut

- La création génère un secret aléatoire inaccessible au créateur, puis envoie le lien d'activation valable 72 heures.
- La route existante `POST /api/auth/activate` reçoit `token`, `newPassword` et `confirmPassword`. Elle active le compte et mémorise son activation.
- Un compte `Pending` ou `Inactive` ne peut pas se connecter. La désactivation révoque aussi ses sessions et ses liens encore utilisables.
- La réactivation d'un compte précédemment activé conserve son mot de passe et ses affectations. Une nouvelle connexion est nécessaire.
- Un compte jamais activé redevient `Pending` lors de sa réactivation et reçoit une nouvelle invitation.
- Le renvoi d'invitation est possible uniquement pour un compte `Pending` et invalide les anciens liens.
- Modifier le courriel d'un compte en attente invalide ses liens précédents et envoie une invitation à la nouvelle adresse. Pour un compte déjà activé, les coordonnées sont modifiées par l'administrateur sans nouvelle activation.
- Modifier le nom d'utilisateur révoque ses sessions. Aucun mot de passe n'est renvoyé par les routes intervenants.
- Un administrateur ne peut désactiver son propre compte ni le dernier administrateur actif. Il peut désactiver un autre administrateur.
- La route historique `/api/auth/dev/generate-activation-token` exige désormais un administrateur actif.

## Équipes et droits

Seuls les administrateurs peuvent utiliser `POST /api/team` et `PATCH /api/team/modify/{teamId}`. Ces routes conservent leur format existant :

```json
{
  "teamName": "Équipe A",
  "sportId": 1,
  "headCoachId": null,
  "subcoachIds": [],
  "kineIds": []
}
```

Modification : `newTeamName`, `newCoachId`, `newSubcoachesIds`, `newKinesiologistsIds`. Les affectations sont remplacées par les listes envoyées; une liste absente ou `null` équivaut à une liste vide. `headCoachId` et `newCoachId` peuvent être `null`. Une personne ne peut pas être à la fois coach principal et adjoint de la même équipe.

Un coach ne possède qu'une équipe : l'affecter à une nouvelle équipe le retire de l'ancienne, qui peut alors ne plus avoir de coach principal. Un kinésiologue peut posséder plusieurs équipes. Affecter ou retirer un intervenant ne change jamais son statut de compte. Les comptes en attente et inactifs peuvent être affectés, mais seuls les comptes actifs exercent leurs droits.

Un coach ou un kinésiologue sans équipe peut se connecter, mais ses listes d'équipes, d'athlètes et de résultats sont vides. Les accès directs hors périmètre sont refusés. Les listes globales de coachs et de kinésiologues destinées à l'affectation sont réservées aux administrateurs.

Coachs et kinésiologues peuvent créer, modifier et désactiver les athlètes de leurs équipes, leur assigner des tests, consulter leurs résultats, les saisir et les approuver/refuser. L'approbation utilise la route existante `PUT /api/result/verify/{testResultId}/{approved}` et enregistre l'intervenant responsable. La désactivation d'un athlète utilise `PUT /api/auth/{userId}/deactivate` et désactive son compte global, même s'il appartient à plusieurs équipes.

Pour modifier un athlète, `teamsInfo` décrit uniquement ses associations souhaitées dans les équipes gérées par l'appelant. Une liste vide retire ces associations. Toutes les associations, positions et disciplines d'autres équipes sont préservées. Les administrateurs remplacent l'ensemble des associations. Les champs personnels de l'athlète et ses résultats sont communs à ses équipes; le modèle existant ne rattache pas un résultat à une équipe spécifique. L'accès à l'athlète via une équipe autorise donc l'accès à son dossier et ses résultats, sans exposer les métadonnées de ses autres équipes.

Les batteries sont filtrées par équipes et leur modification vérifie le périmètre. Le catalogue de tests reste commun; un coach ou kinésiologue affecté peut le consulter pour choisir un test à assigner. Les droits existants de création des tests/batteries (administrateur et coach) sont conservés.

## Base de données et intégration

- `schema.sql` et `data.sql` prennent en compte `account_activated` et `session_version` pour les bases de développement recréées.
- Pour une base existante, appliquer une fois `docs/migrations/20260920_intervenants.sql`. Ne pas utiliser `schema.sql` pour migrer une base à conserver : il supprime et recrée les tables.
- La migration considère les comptes actifs existants comme activés. Faute d'historique fiable, les anciens comptes inactifs devront valider une nouvelle invitation à leur réactivation. Les futurs comptes conservent explicitement leur historique d'activation.
- Les sessions antérieures au déploiement doivent se reconnecter.
- L'envoi réel repose sur la configuration Microsoft Graph existante (`MAIL_ENABLED` et paramètres Azure). Les tests automatisés remplacent le service de courriel; ils n'envoient pas de messages réels.
- L'onglet « Intervenants » et les boutons associés doivent être ajoutés dans le dépôt front-end. Masquer cet onglet pour les utilisateurs non administrateurs; les contrôles côté serveur restent systématiques.

Erreurs : 400 pour une demande invalide, 401 pour une session révoquée, 403 pour un accès interdit, 404 pour un intervenant introuvable, 409 pour un conflit de contrainte en base et 503 si l'envoi du courriel échoue. La création est annulée si l'envoi échoue.
