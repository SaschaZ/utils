package dev.zieger.utils.misc

inline fun <T, U, V> whenNotNull(val0: T?, val1: U?, block: (val0: T, val1: U) -> V): V? =
    if (val0 != null && val1 != null) block(val0, val1) else null

inline fun <T, U, V, W> whenNotNull(val0: T?, val1: U?, val2: V?, block: (val0: T, val1: U, val2: V) -> W): W? =
    if (val0 != null && val1 != null && val2 != null) block(val0, val1, val2) else null

inline fun <T, U, V, W, X> whenNotNull(
    val0: T?,
    val1: U?,
    val2: V?,
    val3: W?,
    block: (val0: T, val1: U, val2: V, val3: W) -> X
): X? =
    if (val0 != null && val1 != null && val2 != null && val3 != null) block(val0, val1, val2, val3) else null

inline fun <A, B, C, D, E, Z> whenNotNull(
    val0: A?, val1: B?, val2: C?, val3: D?, val4: E?,
    block: (val0: A, val1: B, val2: C, val3: D, val4: E) -> Z
): Z? =
    if (val0 != null && val1 != null && val2 != null && val3 != null && val4 != null)
        block(val0, val1, val2, val3, val4) else null

inline fun <A, B, C, D, E, F, Z> whenNotNull(
    val0: A?, val1: B?, val2: C?, val3: D?, val4: E?, val5: F?,
    block: (val0: A, val1: B, val2: C, val3: D, val4: E, val5: F) -> Z
): Z? =
    if (val0 != null && val1 != null && val2 != null && val3 != null && val4 != null && val5 != null)
        block(val0, val1, val2, val3, val4, val5) else null

inline fun <A, B, C, D, E, F, G, Z> whenNotNull(
    val0: A?, val1: B?, val2: C?, val3: D?, val4: E?, val5: F?, val6: G?,
    block: (val0: A, val1: B, val2: C, val3: D, val4: E, val5: F, val6: G) -> Z
): Z? =
    if (val0 != null && val1 != null && val2 != null && val3 != null && val4 != null && val5 != null && val6 != null)
        block(val0, val1, val2, val3, val4, val5, val6) else null

inline fun <A, B, C, D, E, F, G, H, Z> whenNotNull(
    val0: A?, val1: B?, val2: C?, val3: D?, val4: E?, val5: F?, val6: G?, val7: H?,
    block: (val0: A, val1: B, val2: C, val3: D, val4: E, val5: F, val6: G, val7: H) -> Z
): Z? =
    if (val0 != null && val1 != null && val2 != null && val3 != null && val4 != null && val5 != null && val6 != null && val7 != null)
        block(val0, val1, val2, val3, val4, val5, val6, val7) else null

inline fun <A, B, C, D, E, F, G, H, I, Z> whenNotNull(
    val0: A?, val1: B?, val2: C?, val3: D?, val4: E?, val5: F?, val6: G?, val7: H?, val8: I?,
    block: (val0: A, val1: B, val2: C, val3: D, val4: E, val5: F, val6: G, val7: H, val8: I) -> Z
): Z? =
    if (val0 != null && val1 != null && val2 != null && val3 != null && val4 != null && val5 != null && val6 != null && val7 != null && val8 != null)
        block(val0, val1, val2, val3, val4, val5, val6, val7, val8) else null