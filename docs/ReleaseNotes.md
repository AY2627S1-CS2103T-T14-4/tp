---
  layout: default.md
  title: "Release notes"
---

# Release notes

## Unreleased

* Add client appointments with `appt c/CLIENT_INDEX d/DD-MM-YYYY t/HH:mm`. Appointments use the currently displayed client list, require a strictly future date/time, reject duplicate slots for the same client, appear under their client, and survive restart. Different clients may share a slot, and clients do not need recorded pets.
