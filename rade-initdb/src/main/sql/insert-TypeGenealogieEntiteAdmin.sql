/*  This file is part of the Rade project (https://github.com/mgimpel/rade).
 *  Copyright (C) 2018 Marc Gimpel
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
/* $Id$ */


INSERT INTO ZR_TYPEGENEALOGIE("CODE", "LIBELLE_COURT", "LIBELLE_LONG", "STATUT_DEFAUT") VALUES ('10','Changement de nom','Changement de nom','I');
INSERT INTO ZR_TYPEGENEALOGIE("CODE", "LIBELLE_COURT", "LIBELLE_LONG", "STATUT_DEFAUT") VALUES ('20','Création','Création','I');
INSERT INTO ZR_TYPEGENEALOGIE("CODE", "LIBELLE_COURT", "LIBELLE_LONG", "STATUT_DEFAUT") VALUES ('21','Rétablissement','Rétablissement','I')	;
INSERT INTO ZR_TYPEGENEALOGIE("CODE", "LIBELLE_COURT", "LIBELLE_LONG", "STATUT_DEFAUT") VALUES ('30','Suppression','Suppression','I');
INSERT INTO ZR_TYPEGENEALOGIE("CODE","LIBELLE_COURT","LIBELLE_LONG","STATUT_DEFAUT" ) VALUES ('31','Fusion','Fusion simple','I');
INSERT INTO ZR_TYPEGENEALOGIE("CODE","LIBELLE_COURT","LIBELLE_LONG","STATUT_DEFAUT") VALUES ('32','Fusion','Création de commune nouvelle','I' );
INSERT INTO ZR_TYPEGENEALOGIE("CODE","LIBELLE_COURT","LIBELLE_LONG","STATUT_DEFAUT" ) VALUES ('33','Fusion','Fusion association','I');
INSERT INTO ZR_TYPEGENEALOGIE("CODE","LIBELLE_COURT","LIBELLE_LONG","STATUT_DEFAUT") VALUES ('34','Fusion','Transformation de fusion association en fusion simple','I');
INSERT INTO ZR_TYPEGENEALOGIE("CODE","LIBELLE_COURT","LIBELLE_LONG","STATUT_DEFAUT") VALUES ('41','Changement de dept.','Changement de code dû à un changement de département','I');
INSERT INTO ZR_TYPEGENEALOGIE("CODE","LIBELLE_COURT","LIBELLE_LONG","STATUT_DEFAUT") VALUES ('50','Transfert chef-lieu','Changement de code dû à un transfert de chef-lieu','I');
INSERT INTO ZR_TYPEGENEALOGIE("CODE","LIBELLE_COURT","LIBELLE_LONG","STATUT_DEFAUT") VALUES ('70','Changement de code','Transformation de commune associé en commune déléguée','I');

-- Code INSEE
INSERT INTO ZR_TYPEGENEALOGIE("CODE", "LIBELLE_COURT", "LIBELLE_LONG", "STATUT_DEFAUT") VALUES ('100', 'Changement de nom', 'Changement de nom', 'I');
INSERT INTO ZR_TYPEGENEALOGIE("CODE", "LIBELLE_COURT", "LIBELLE_LONG", "STATUT_DEFAUT") VALUES ('110', 'Changement de nom', 'Changement de nom dû à une fusion (simple ou association)', 'I');
INSERT INTO ZR_TYPEGENEALOGIE("CODE", "LIBELLE_COURT", "LIBELLE_LONG", "STATUT_DEFAUT") VALUES ('111', 'Changement de nom', 'Changement de nom (création de commune nouvelle)', 'I');
INSERT INTO ZR_TYPEGENEALOGIE("CODE", "LIBELLE_COURT", "LIBELLE_LONG", "STATUT_DEFAUT") VALUES ('120', 'Changement de nom', 'Changement de nom dû à un rétablissement', 'I');
INSERT INTO ZR_TYPEGENEALOGIE("CODE", "LIBELLE_COURT", "LIBELLE_LONG", "STATUT_DEFAUT") VALUES ('130', 'Changement de nom', 'Changement de nom dû au changement de chef-lieu', 'I');
INSERT INTO ZR_TYPEGENEALOGIE("CODE", "LIBELLE_COURT", "LIBELLE_LONG", "STATUT_DEFAUT") VALUES ('140', 'Changement de nom', 'Changement de nom dû au transfert du bureau centralisateur de canton', 'I');
INSERT INTO ZR_TYPEGENEALOGIE("CODE", "LIBELLE_COURT", "LIBELLE_LONG", "STATUT_DEFAUT") VALUES ('150', 'Changement de nom', 'Changement de nom dû au transfert du chef-lieu d''arrondissement', 'I');
INSERT INTO ZR_TYPEGENEALOGIE("CODE", "LIBELLE_COURT", "LIBELLE_LONG", "STATUT_DEFAUT") VALUES ('200', 'Création', 'Création', 'I');
INSERT INTO ZR_TYPEGENEALOGIE("CODE", "LIBELLE_COURT", "LIBELLE_LONG", "STATUT_DEFAUT") VALUES ('210', 'Rétablissement', 'Rétablissement', 'I');
INSERT INTO ZR_TYPEGENEALOGIE("CODE", "LIBELLE_COURT", "LIBELLE_LONG", "STATUT_DEFAUT") VALUES ('220', 'Commune parcellé', 'Commune ayant donné des parcelles pour la création', 'I');
INSERT INTO ZR_TYPEGENEALOGIE("CODE", "LIBELLE_COURT", "LIBELLE_LONG", "STATUT_DEFAUT") VALUES ('230', 'Commune se séparant', 'Commune se séparant', 'I');
INSERT INTO ZR_TYPEGENEALOGIE("CODE", "LIBELLE_COURT", "LIBELLE_LONG", "STATUT_DEFAUT") VALUES ('240', 'Création', 'Création d''une fraction cantonale', 'N');
INSERT INTO ZR_TYPEGENEALOGIE("CODE", "LIBELLE_COURT", "LIBELLE_LONG", "STATUT_DEFAUT") VALUES ('300', 'Suppression', 'Suppression commune suite à partition de territoire', 'I');
INSERT INTO ZR_TYPEGENEALOGIE("CODE", "LIBELLE_COURT", "LIBELLE_LONG", "STATUT_DEFAUT") VALUES ('310', 'Fusion', 'Fusion : commune absorbée', 'I');
INSERT INTO ZR_TYPEGENEALOGIE("CODE", "LIBELLE_COURT", "LIBELLE_LONG", "STATUT_DEFAUT") VALUES ('311', 'Fusion', 'Commune nouvelle : commune non déléguée', 'I');
INSERT INTO ZR_TYPEGENEALOGIE("CODE", "LIBELLE_COURT", "LIBELLE_LONG", "STATUT_DEFAUT") VALUES ('312', 'Fusion', 'Commune nouvelle : commune préexistente non déléguée restant non déléguée (commune-pôle)', 'I');
INSERT INTO ZR_TYPEGENEALOGIE("CODE", "LIBELLE_COURT", "LIBELLE_LONG", "STATUT_DEFAUT") VALUES ('320', 'Fusion', 'Fusion : commune absorbante', 'I');
INSERT INTO ZR_TYPEGENEALOGIE("CODE", "LIBELLE_COURT", "LIBELLE_LONG", "STATUT_DEFAUT") VALUES ('321', 'Fusion', 'Commune nouvelle sans déléguée : commune-pôle', 'I');
INSERT INTO ZR_TYPEGENEALOGIE("CODE", "LIBELLE_COURT", "LIBELLE_LONG", "STATUT_DEFAUT") VALUES ('330', 'Fusion', 'Fusion - association : commune associée', 'I');
INSERT INTO ZR_TYPEGENEALOGIE("CODE", "LIBELLE_COURT", "LIBELLE_LONG", "STATUT_DEFAUT") VALUES ('331', 'Fusion', 'Commune nouvelle : commune déléguée', 'I');
INSERT INTO ZR_TYPEGENEALOGIE("CODE", "LIBELLE_COURT", "LIBELLE_LONG", "STATUT_DEFAUT") VALUES ('332', 'Fusion', 'Commune nouvelle : commune préexistante associée devenant déléguée', 'I');
INSERT INTO ZR_TYPEGENEALOGIE("CODE", "LIBELLE_COURT", "LIBELLE_LONG", "STATUT_DEFAUT") VALUES ('333', 'Fusion', 'Commune nouvelle : commune préexistante déléguée restant déléguée', 'I');
INSERT INTO ZR_TYPEGENEALOGIE("CODE", "LIBELLE_COURT", "LIBELLE_LONG", "STATUT_DEFAUT") VALUES ('340', 'Fusion', 'Fusion-association : commune absorbante', 'I');
INSERT INTO ZR_TYPEGENEALOGIE("CODE", "LIBELLE_COURT", "LIBELLE_LONG", "STATUT_DEFAUT") VALUES ('341', 'Fusion', 'Commune nouvelle avec déléguée : commune-pôle', 'I');
INSERT INTO ZR_TYPEGENEALOGIE("CODE", "LIBELLE_COURT", "LIBELLE_LONG", "STATUT_DEFAUT") VALUES ('350', 'Fusion', 'Fusion-association se transformant en fusion simple (commune absorbée)', 'I');
INSERT INTO ZR_TYPEGENEALOGIE("CODE", "LIBELLE_COURT", "LIBELLE_LONG", "STATUT_DEFAUT") VALUES ('351', 'Fusion', 'Commune nouvelle : suppression de commune préexistante', 'I');
INSERT INTO ZR_TYPEGENEALOGIE("CODE", "LIBELLE_COURT", "LIBELLE_LONG", "STATUT_DEFAUT") VALUES ('360', 'Fusion', 'Fusion-association se transformant en fusion simple : commune-pôle', 'I');
INSERT INTO ZR_TYPEGENEALOGIE("CODE", "LIBELLE_COURT", "LIBELLE_LONG", "STATUT_DEFAUT") VALUES ('370', 'Suppression', 'Suppression de la fraction cantonale', 'N');
INSERT INTO ZR_TYPEGENEALOGIE("CODE", "LIBELLE_COURT", "LIBELLE_LONG", "STATUT_DEFAUT") VALUES ('390', 'Suppression', 'Commune ayant reçu des parcelles suite à une suppression', 'N');
INSERT INTO ZR_TYPEGENEALOGIE("CODE", "LIBELLE_COURT", "LIBELLE_LONG", "STATUT_DEFAUT") VALUES ('400', 'Changement de région', 'Changement de région', 'N');
INSERT INTO ZR_TYPEGENEALOGIE("CODE", "LIBELLE_COURT", "LIBELLE_LONG", "STATUT_DEFAUT") VALUES ('410', 'Changement de dept.', 'Changement de département', 'N');
INSERT INTO ZR_TYPEGENEALOGIE("CODE", "LIBELLE_COURT", "LIBELLE_LONG", "STATUT_DEFAUT") VALUES ('411', 'Changement de dept.', 'Changement de département (lors de création de commune nouvelle)', 'N');
INSERT INTO ZR_TYPEGENEALOGIE("CODE", "LIBELLE_COURT", "LIBELLE_LONG", "STATUT_DEFAUT") VALUES ('420', 'Changement d''arr.', 'Changement d''arrondissement', 'N');
INSERT INTO ZR_TYPEGENEALOGIE("CODE", "LIBELLE_COURT", "LIBELLE_LONG", "STATUT_DEFAUT") VALUES ('421', 'Changement d''arr.', 'Changement d''arrondissement (lors de création de commune nouvelle)', 'N');
INSERT INTO ZR_TYPEGENEALOGIE("CODE", "LIBELLE_COURT", "LIBELLE_LONG", "STATUT_DEFAUT") VALUES ('430', 'Changement de canton', 'Changement de canton', 'N');
INSERT INTO ZR_TYPEGENEALOGIE("CODE", "LIBELLE_COURT", "LIBELLE_LONG", "STATUT_DEFAUT") VALUES ('431', 'Changement de canton', 'Changement de canton (lors de création de commune nouvelle)', 'N');
INSERT INTO ZR_TYPEGENEALOGIE("CODE", "LIBELLE_COURT", "LIBELLE_LONG", "STATUT_DEFAUT") VALUES ('500', 'Transfert chef-lieu', 'Transfert de chef-lieu de commune', 'N');
INSERT INTO ZR_TYPEGENEALOGIE("CODE", "LIBELLE_COURT", "LIBELLE_LONG", "STATUT_DEFAUT") VALUES ('510', 'Transfert chef-lieu', 'Transfert de bureau centralisateur de canton', 'N');
INSERT INTO ZR_TYPEGENEALOGIE("CODE", "LIBELLE_COURT", "LIBELLE_LONG", "STATUT_DEFAUT") VALUES ('520', 'Transfert chef-lieu', 'Transfert de chef-lieu d''arrondissement', 'N');
INSERT INTO ZR_TYPEGENEALOGIE("CODE", "LIBELLE_COURT", "LIBELLE_LONG", "STATUT_DEFAUT") VALUES ('530', 'Transfert chef-lieu', 'Transfert de chef-lieu de département', 'N');
INSERT INTO ZR_TYPEGENEALOGIE("CODE", "LIBELLE_COURT", "LIBELLE_LONG", "STATUT_DEFAUT") VALUES ('540', 'Transfert chef-lieu', 'Transfert de chef-lieu de région', 'N');
INSERT INTO ZR_TYPEGENEALOGIE("CODE", "LIBELLE_COURT", "LIBELLE_LONG", "STATUT_DEFAUT") VALUES ('600', 'Cession de parcelles', 'Cession de parcelles avec incidence démographique', 'I');
INSERT INTO ZR_TYPEGENEALOGIE("CODE", "LIBELLE_COURT", "LIBELLE_LONG", "STATUT_DEFAUT") VALUES ('610', 'Cession de parcelles', 'Cession de parcelles sans incidence démographique', 'I');
INSERT INTO ZR_TYPEGENEALOGIE("CODE", "LIBELLE_COURT", "LIBELLE_LONG", "STATUT_DEFAUT") VALUES ('620', 'Récept. de parcelles', 'Réception de parcelles avec incidence démographique', 'I');
INSERT INTO ZR_TYPEGENEALOGIE("CODE", "LIBELLE_COURT", "LIBELLE_LONG", "STATUT_DEFAUT") VALUES ('630', 'Récept. de parcelles', 'Réception de parcelles sans incidence démographique', 'I');
INSERT INTO ZR_TYPEGENEALOGIE("CODE", "LIBELLE_COURT", "LIBELLE_LONG", "STATUT_DEFAUT") VALUES ('700', 'Changement de code', 'Commune associée devenant déléguée (hors création commune nouvelle)', 'I');
INSERT INTO ZR_TYPEGENEALOGIE("CODE", "LIBELLE_COURT", "LIBELLE_LONG", "STATUT_DEFAUT") VALUES ('990', 'Erreur', 'Numéro ancien créé par erreur', 'N');
-- Code AESN
INSERT INTO ZR_TYPEGENEALOGIE("CODE", "LIBELLE_COURT", "LIBELLE_LONG", "STATUT_DEFAUT") VALUES ('X00', 'Correction manuelle', 'Correction manuelle', 'P');
INSERT INTO ZR_TYPEGENEALOGIE("CODE", "LIBELLE_COURT", "LIBELLE_LONG", "STATUT_DEFAUT") VALUES ('X01', 'Changement anticipé', 'Changement anticipé', 'P');
INSERT INTO ZR_TYPEGENEALOGIE("CODE", "LIBELLE_COURT", "LIBELLE_LONG", "STATUT_DEFAUT") VALUES ('X10', 'Changement de nom', 'Changement de l''orthographe de la Commune', 'P');
INSERT INTO ZR_TYPEGENEALOGIE("CODE", "LIBELLE_COURT", "LIBELLE_LONG", "STATUT_DEFAUT") VALUES ('X20', 'Création', 'Création de la Commune', 'P');
INSERT INTO ZR_TYPEGENEALOGIE("CODE", "LIBELLE_COURT", "LIBELLE_LONG", "STATUT_DEFAUT") VALUES ('X30', 'Suppression', 'Suppression de la Commune', 'P');


