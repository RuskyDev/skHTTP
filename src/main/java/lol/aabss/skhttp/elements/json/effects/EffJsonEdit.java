package lol.aabss.skhttp.elements.json.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.UnparsedLiteral;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import lol.aabss.skhttp.SkHttp;
import lol.aabss.skhttp.objects.Json;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Name("Json - Json Edit")
@Description("Adds/Removes elements from a json object/array")
@Examples({
        "add key \"uuid\" value random uuid in {_json}",
        "remove all 123 in {_jsonObject}",
        "remove object at index 1 in {_jsonArray}"
})
@Since("1.4")
public class EffJsonEdit extends Effect {

    static {
        if (SkHttp.instance.getConfig().getBoolean("json-elements", true)) {
            Skript.registerEffect(EffJsonEdit.class,
                    "(put|add) [[key] %-string% [and]] [value] %object% in %jsonobjects/jsonarrays%",
                    "remove [:first|all] %object% in %jsonarrays/jsonobjects%",
                    "remove [object at] index [of] %integer% in %jsonarrays/jsonobjects%"
            );
        }
    }

    private int matchedPattern;
    private Expression<String> key;
    private Expression<Object> value;
    private Expression<JsonElement> json;
    private boolean first;
    private Expression<Integer> index;

    @Override
    protected void execute(@NotNull Event e) {
        for (JsonElement element : json.getArray(e)){
            if (matchedPattern == 0){
                if (this.value == null){
                    return;
                }
                Object value = this.value.getSingle(e);
                if (value == null){
                    return;
                }
                if (key == null){
                    Json json = new Json(element, e);
                    json.add(null, value, e);
                } else {
                    String key = this.key.getSingle(e);
                    if (key == null) {
                        return;
                    }
                    Json json = new Json(element, e);
                    json.add(key, value, e);
                }
            } else if (matchedPattern == 1){
                if (this.value == null){
                    return;
                }
                Object value = this.value.getSingle(e);
                if (value == null){
                    return;
                }
                Json json = new Json(element, e);
                if (first){
                    json.removeFirst(value, e);
                } else {
                    json.removeAll(value, e);
                }
            } else if (matchedPattern == 2){
                if (this.index == null){
                    return;
                }
                Integer index = this.index.getSingle(e);
                if (index == null){
                    return;
                }
                new Json(element, e).removeIndex(index);
            }
        }
    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        return "json edit";
    }

    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.@NotNull ParseResult parseResult) {
        this.matchedPattern = matchedPattern;
        if (matchedPattern == 0){
            key = (Expression<String>) exprs[0];
            value = (Expression<Object>) exprs[1];
            json = (Expression<JsonElement>) exprs[2];
        } else if (matchedPattern == 1){
            first = parseResult.hasTag("first");
            value = (Expression<Object>) exprs[0];
            json = (Expression<JsonElement>) exprs[1];
        } else if (matchedPattern == 2){
            index = (Expression<Integer>) exprs[0];
            json = (Expression<JsonElement>) exprs[1];
        }
        if (this.value instanceof UnparsedLiteral) {
            value = LiteralUtils.defendExpression(value);
        }
        if (this.value != null) {
            return LiteralUtils.canInitSafely(value);
        }
        return true;
    }
}

