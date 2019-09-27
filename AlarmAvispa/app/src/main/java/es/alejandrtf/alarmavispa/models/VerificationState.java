package es.alejandrtf.alarmavispa.models;

import android.content.Context;
import android.content.res.Resources;

public enum VerificationState {
    ACTIVE,
    REMOVED,
    INACTIVE,
    NOT_VELUTINA,
    NOT_LOCATED;

    /**
     * Returns a localized label used to represent this enumeration value.  If no label
     * has been defined, then this defaults to the result of {@link Enum#name()}.
     *
     * <p>The name of the string resource for the label must match the name of the enumeration
     * value.  For example, for enum value 'ENUM1' the resource would be defined as 'R.string.ENUM1'.
     *
     * @param context   the context that the string resource of the label is in.
     * @return      a localized label for the enum value or the result of name()
     */
    public String getLocalizedName(Context context) {
        Resources res = context.getResources();
        int resId = res.getIdentifier(this.name(), "string", context.getPackageName());
        if (resId !=0) {
            return (res.getString(resId));
        }
        return (name());
    }
}
