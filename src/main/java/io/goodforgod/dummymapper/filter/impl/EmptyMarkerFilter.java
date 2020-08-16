package io.goodforgod.dummymapper.filter.impl;

import io.goodforgod.dummymapper.marker.*;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

/**
 * Filters out empty {@link RawMarker} or if any {@link CollectionMarker} or {@link MapMarker} have empty {@link RawMarker}
 *
 * @author Anton Kurako (GoodforGod)
 * @since 13.8.2020
 */
public class EmptyMarkerFilter extends BaseFilter {

    @NotNull
    @Override
    public RawMarker filter(@NotNull RawMarker marker) {
        if(marker.isEmpty())
            return RawMarker.EMPTY;

        final HashMap<String, Marker> structure = new HashMap<>(marker.getStructure());
        structure.forEach((k, v) -> {
            if (v instanceof RawMarker && ((RawMarker) v).getStructure().isEmpty()) {
                marker.getStructure().remove(k);
            } else if (v instanceof CollectionMarker
                    && ((CollectionMarker) v).isRaw()
                    && ((RawMarker) ((CollectionMarker) v).getErasure()).getStructure().isEmpty()) {
                marker.getStructure().remove(k);
            } else if (v instanceof ArrayMarker
                    && ((ArrayMarker) v).isRaw()
                    && ((RawMarker) ((ArrayMarker) v).getErasure()).getStructure().isEmpty()) {
                marker.getStructure().remove(k);
            } else if (v instanceof MapMarker && ((MapMarker) v).isRaw()) {
                if (((MapMarker) v).getKeyErasure() instanceof RawMarker
                        && ((RawMarker) ((MapMarker) v).getKeyErasure()).getStructure().isEmpty())
                    marker.getStructure().remove(k);

                if (((MapMarker) v).getValueErasure() instanceof RawMarker
                        && ((RawMarker) ((MapMarker) v).getValueErasure()).getStructure().isEmpty())
                    marker.getStructure().remove(k);
            }
        });

        return filterRecursive(marker);
    }
}
