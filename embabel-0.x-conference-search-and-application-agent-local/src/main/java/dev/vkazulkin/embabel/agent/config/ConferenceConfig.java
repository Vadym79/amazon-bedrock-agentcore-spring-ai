package dev.vkazulkin.embabel.agent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import com.embabel.agent.api.common.Actor;
import com.embabel.agent.prompt.persona.RoleGoalBackstory;

@Validated
@ConfigurationProperties(prefix = "conference")
public record ConferenceConfig(Actor<RoleGoalBackstory> attendee, Actor<RoleGoalBackstory> speaker) {
}
