package kr.toxicity.model.api.data.renderer;

import com.mojang.authlib.GameProfile;
import kr.toxicity.model.api.BetterModel;
import kr.toxicity.model.api.bone.BoneName;
import kr.toxicity.model.api.data.blueprint.BlueprintAnimation;
import kr.toxicity.model.api.data.blueprint.ModelBlueprint;
import kr.toxicity.model.api.tracker.DummyTracker;
import kr.toxicity.model.api.tracker.EntityTracker;
import kr.toxicity.model.api.tracker.TrackerModifier;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A blueprint renderer.
 */
@RequiredArgsConstructor
public final class ModelRenderer {
    @Getter
    private final ModelBlueprint parent;
    @Getter
    @Unmodifiable
    private final Map<BoneName, RendererGroup> rendererGroupMap;
    private final Map<String, BlueprintAnimation> animationMap;

    /**
     * Gets a renderer group by tree
     * @param name part name
     * @return group or null
     */
    public @Nullable RendererGroup groupByTree(@NotNull BoneName name) {
        return groupByTree0(rendererGroupMap, name);
    }

    private static @Nullable RendererGroup groupByTree0(@NotNull Map<BoneName, RendererGroup> map, @NotNull BoneName name) {
        if (map.isEmpty()) return null;
        var get = map.get(name);
        if (get != null) return get;
        else return map.values()
                .stream()
                .map(g -> groupByTree0(g.getChildren(), name))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    /**
     * Gets all names of animation.
     * @return names
     */
    public @NotNull @Unmodifiable Set<String> animations() {
        return Collections.unmodifiableSet(animationMap.keySet());
    }

    /**
     * Gets model's name.
     * @return name
     */
    public @NotNull String name() {
        return parent.name();
    }



    /**
     * Creates tracker by location
     * @param location location
     * @return empty tracker
     */
    public @NotNull DummyTracker create(@NotNull Location location) {
        return create(location, TrackerModifier.DEFAULT);
    }
    /**
     * Creates tracker by entity
     * @param entity entity
     * @return entity tracker
     */
    public @NotNull EntityTracker create(@NotNull Entity entity) {
        return create(entity, TrackerModifier.DEFAULT);
    }

    /**
     * Gets or creates tracker by entity
     * @param entity entity
     * @return entity tracker
     */
    public @NotNull EntityTracker getOrCreate(@NotNull Entity entity) {
        return getOrCreate(entity, TrackerModifier.DEFAULT);
    }

    /**
     * Gets or creates tracker by entity
     * @param entity entity
     * @param modifier modifier
     * @return entity tracker
     */
    public @NotNull EntityTracker getOrCreate(@NotNull Entity entity, @NotNull TrackerModifier modifier) {
        var tracker = EntityTracker.tracker(entity.getUniqueId());
        if (tracker != null) return tracker;
        return create(entity, modifier);
    }

    /**
     * Creates tracker by location
     * @param location location
     * @param modifier modifier
     * @return empty tracker
     */
    public @NotNull DummyTracker create(@NotNull Location location, @NotNull TrackerModifier modifier) {
        var source = RenderSource.of(location);
        return source.create(
                instance(source, location, modifier),
                modifier
        );
    }
    /**
     * Creates tracker by location and player
     * @param location location
     * @param player player
     * @return empty tracker
     */
    public @NotNull DummyTracker create(@NotNull Location location, @NotNull Player player) {
        return create(location, player, TrackerModifier.DEFAULT);
    }

    /**
     * Creates tracker by location and profile
     * @param location location
     * @param profile profile
     * @return empty tracker
     */
    public @NotNull DummyTracker create(@NotNull Location location, @NotNull GameProfile profile) {
        return create(location, profile, TrackerModifier.DEFAULT);
    }

    /**
     * Creates tracker by location and profile
     * @param location location
     * @param profile profile
     * @param slim slim
     * @return empty tracker
     */
    public @NotNull DummyTracker create(@NotNull Location location, @NotNull GameProfile profile, boolean slim) {
        return create(location, profile, slim, TrackerModifier.DEFAULT);
    }

    /**
     * Creates tracker by location and player
     * @param location location
     * @param player player
     * @param modifier modifier
     * @return empty tracker
     */
    public @NotNull DummyTracker create(@NotNull Location location, @NotNull Player player, @NotNull TrackerModifier modifier) {
        var channel = BetterModel.plugin().playerManager().player(player.getUniqueId());
        return channel == null ? create(location, BetterModel.plugin().nms().profile(player), modifier) : create(location, channel.profile(), channel.isSlim(), modifier);
    }

    /**
     * Creates tracker by location and profile
     * @param location location
     * @param profile profile
     * @param modifier modifier
     * @return empty tracker
     */
    public @NotNull DummyTracker create(@NotNull Location location, @NotNull GameProfile profile, @NotNull TrackerModifier modifier) {
        return create(location, profile, BetterModel.plugin().skinManager().isSlim(profile), modifier);
    }

    /**
     * Creates tracker by location and profile
     * @param location location
     * @param profile profile
     * @param slim slim
     * @param modifier modifier
     * @return empty tracker
     */
    public @NotNull DummyTracker create(@NotNull Location location, @NotNull GameProfile profile, boolean slim, @NotNull TrackerModifier modifier) {
        var source = RenderSource.of(location, profile, slim);
        return source.create(
                instance(source, location, modifier),
                modifier
        );
    }

    /**
     * Creates tracker by entity
     * @param entity entity
     * @param modifier modifier
     * @return entity tracker
     */
    public @NotNull EntityTracker create(@NotNull Entity entity, @NotNull TrackerModifier modifier) {
        var source = RenderSource.of(entity);
        return source.create(
                instance(source, entity.getLocation().add(0, -1024, 0), modifier),
                modifier
        );
    }

    private @NotNull RenderInstance instance(@NotNull RenderSource source, @NotNull Location location, @NotNull TrackerModifier modifier) {
        return new RenderInstance(this, source, rendererGroupMap
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().create(source, modifier, location))), animationMap);
    }
}
