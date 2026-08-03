# Legal UX Flow

On authenticated session restore, the client reads `pendingLegalDocuments`. A non-empty list opens a non-dismissible modal across protected application routes. The client loads active document metadata, shows links and versions, collects explicit confirmation, posts the listed codes, then refreshes the session. A failed write leaves the gate active and self-heals on the next restore.
