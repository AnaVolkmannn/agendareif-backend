package com.reif.agenda_api.service;

import com.reif.agenda_api.model.Professional;

public record ProfessionalRegistrationResult(Professional professional, String temporaryPassword) {
}