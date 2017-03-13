package com.kniost.criminalintent.database;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.kniost.criminalintent.Crime;
import com.kniost.criminalintent.database.CrimeDbSchema.CrimeTable.Cols;

import java.util.Date;
import java.util.UUID;

/**
 * Created by kniost on 17/1/8.
 */

public class CrimeCursorWrapper extends CursorWrapper {
    public CrimeCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Crime getCrime() {
        String uuidString = getString(getColumnIndex(Cols.UUID));
        String title = getString(getColumnIndex(Cols.TITLE));
        long date = getLong(getColumnIndex(Cols.DATE));
        int isSolved = getInt(getColumnIndex(Cols.SOLVED));
        String suspect = getString(getColumnIndex(Cols.SUSPECT));

        Crime crime = new Crime(UUID.fromString(uuidString));
        crime.setTitle(title);
        crime.setDate(new Date(date));
        crime.setSolved(isSolved != 0);
        crime.setSuspect(suspect);

        return crime;
    }
}
