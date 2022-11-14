package ru.centhis.songbook.data;

import androidx.annotation.NonNull;

public enum TestChord {

    C("C"),
    Csharp("C#"),
    D("D"),
    Dsharp("D#"),
    E("E"),
    F("F"),
    Fsharp("F#"),
    G("G"),
    Gsharp("G#"),
    A("A"),
    Asharp("A#"),
    B("B"),

    Cm("Cm"),
    Csharpm("C#m"),
    Dm("Dm"),
    Dsharpm("D#m"),
    Em("Em"),
    Fm("Fm"),
    Fsharpm("F#m"),
    Gm("Gm"),
    Gsharpm("G#m"),
    Am("Am"),
    Asharpm("A#m"),
    Bm("Bm");

    public final String name;
    private TestChord increaseChord;
    private TestChord decreaseChord;

    TestChord(String name) {
        this.name = name;
    }

    static {
        C.increaseChord = Csharp;
        Csharp.increaseChord = D;
        D.increaseChord = Dsharp;
        Dsharp.increaseChord = E;
        E.increaseChord = F;
        F.increaseChord = Fsharp;
        Fsharp.increaseChord = G;
        G.increaseChord = Gsharp;
        Gsharp.increaseChord = A;
        A.increaseChord = Asharp;
        Asharp.increaseChord = B;
        B.increaseChord = C;

        C.decreaseChord = B;
        Csharp.decreaseChord = C;
        D.decreaseChord = Dsharp;
        Dsharp.decreaseChord = D;
        E.decreaseChord = Dsharp;
        F.decreaseChord = E;
        Fsharp.decreaseChord = F;
        G.decreaseChord = Fsharp;
        Gsharp.decreaseChord = G;
        A.decreaseChord = Gsharp;
        Asharp.decreaseChord = A;
        B.decreaseChord = Asharp;

        Cm.increaseChord = Csharpm;
        Csharpm.increaseChord = Dm;
        Dm.increaseChord = Dsharpm;
        Dsharpm.increaseChord = Em;
        Em.increaseChord = Fm;
        Fm.increaseChord = Fsharpm;
        Fsharpm.increaseChord = Gm;
        Gm.increaseChord = Gsharpm;
        Gsharpm.increaseChord = Am;
        Am.increaseChord = Asharpm;
        Asharpm.increaseChord = Bm;
        Bm.increaseChord = Cm;

        Cm.decreaseChord = Bm;
        Csharpm.decreaseChord = Cm;
        Dm.decreaseChord = Dsharpm;
        Dsharpm.decreaseChord = Dm;
        Em.decreaseChord = Dsharpm;
        Fm.decreaseChord = Em;
        Fsharpm.decreaseChord = Fm;
        Gm.decreaseChord = Fsharpm;
        Gsharpm.decreaseChord = Gm;
        Am.decreaseChord = Gsharpm;
        Asharpm.decreaseChord = Am;
        Bm.decreaseChord = Asharpm;
    }


    @NonNull
    @Override
    public String toString() {
        return this.name;
//        return super.toString();
    }


    public TestChord getIncreaseChord(){
        return this.increaseChord;
    }

    public TestChord getDecreaseChord(){
        return this.decreaseChord;
    }
}
