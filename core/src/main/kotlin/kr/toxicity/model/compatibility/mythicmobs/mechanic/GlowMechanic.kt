package kr.toxicity.model.compatibility.mythicmobs.mechanic

import io.lumine.mythic.api.config.MythicLineConfig
import io.lumine.mythic.api.skills.INoTargetSkill
import io.lumine.mythic.api.skills.SkillMetadata
import io.lumine.mythic.api.skills.SkillResult
import io.lumine.mythic.bukkit.MythicBukkit
import io.lumine.mythic.core.skills.SkillMechanic
import kr.toxicity.model.compatibility.mythicmobs.*

class GlowMechanic(mlc: MythicLineConfig) : SkillMechanic(MythicBukkit.inst().skillManager, null, "", mlc), INoTargetSkill {

    private val predicate = mlc.bonePredicateNullable
    private val glow = mlc.toPlaceholderBoolean(arrayOf("glow", "g"), true)
    private val color = mlc.toPlaceholderColor(arrayOf("color", "c"))

    override fun cast(p0: SkillMetadata): SkillResult {
        val args = p0.toPlaceholderArgs()
        return p0.toTracker()?.let {
            if (it.glow(predicate(args), glow(args), color(args))) it.forceUpdate(true)
            SkillResult.SUCCESS
        } ?: SkillResult.CONDITION_FAILED
    }
}