package de.gapps.utils.misc

infix fun <A, B, C> C.to(pair: Pair<A, B>) = Triple(this, pair.first, pair.second)
infix fun <A, B, C> Pair<A, B>.to(third: C) = Triple(first, second, third)