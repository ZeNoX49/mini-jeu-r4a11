package fr.app.mini_jeu_r4a11.game.data.resources

data class items(
    var name: String = "",          //  le nom de le ressource
    var description: String = "",   //  la description de la ressource
    var color: String = "",         //  la couleur de la ressource
    var explosiveness: Int = 0,     //  l'explosivité de la ressource (en %))
    var flammability: Int = 0,      //  la flammabilité de la ressource (en %))
    var radioactivity: Int = 0,     //  la radioactivité de la ressource (en %))
    var charge: Int = 0,            //  la charge électrique de la ressource (en %)) / 100
    var hardness: Int = 0,          //  la dureté de la ressource (en %))
    var cost: Int = 0,              //  le cout de la ressource (en %))
    var parent: String = "",        //  le nom de la ressource parent (pour la recherche)
)
