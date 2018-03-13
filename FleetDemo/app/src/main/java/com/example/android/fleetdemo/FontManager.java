package com.example.android.fleetdemo;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.res.ResourcesCompat;

import java.util.Hashtable;

/**
 * Created by Azuga on 27-02-2018.
 */
public class FontManager {

    private static final Hashtable<Integer, Typeface> cache = new Hashtable<Integer, Typeface>();

    public static Typeface getTypeface(Context context, TypeFaceEnum typeFaceEnum) {
        Typeface typeface = cache.get(typeFaceEnum.getName());
        if (typeface == null) {
            if (context == null || typeFaceEnum == null) {
                return null;
            }

            typeface = ResourcesCompat.getFont(context,typeFaceEnum.getName());

            cache.put(typeFaceEnum.getName(), typeface);
        }

        return typeface;
    }

    public enum TypeFaceEnum {
        PROXIMANOVA(0, R.font.proximanova_semibold),
        PROXIMANOVA_BOLD(1, R.font.proximanova_semibold),
        PROXIMANOVA_SEMI_BOLD(2, R.font.proximanova_semibold),
        PROXIMANOVA_ITALIC(3, R.font.proximanova_semibold),
        PROXIMANOVA_REGULAR(4, R.font.proximanova_semibold),
        PROXIMANOVA_LITE(5, R.font.proximanova_semibold),
        ROBOTO_REGULAR(6, R.font.proximanova_semibold);

        private final int name;
        private final int id;

        private TypeFaceEnum(int id, int name) {
            this.name = name;
            this.id = id;
        }

        public int getName() {
            return this.name;
        }

        public int getId() {
            return this.id;
        }

        public static TypeFaceEnum enumForId(int enumId) {
            switch (enumId) {
                case 0:
                    return PROXIMANOVA;
                case 1:
                    return PROXIMANOVA_BOLD;
                case 2:
                    return PROXIMANOVA_SEMI_BOLD;
                case 3:
                    return PROXIMANOVA_ITALIC;
                case 4:
                    return PROXIMANOVA_REGULAR;
                case 5:
                    return PROXIMANOVA_LITE;
                case 6: return ROBOTO_REGULAR;
            }

            return null;
        }
    }
}