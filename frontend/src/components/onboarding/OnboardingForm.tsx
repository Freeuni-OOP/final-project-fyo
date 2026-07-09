import { useState, useEffect, type CSSProperties } from "react";
import { type OnboardingPayload, type UserSportPayload } from "../../api/Onboarding";
import { type SportDto } from "../../api/Sports";
import "./OnboardingForm.css";

interface Props {
    sports: SportDto[];
    /** Prefill from signup so we don't re-ask first/last name. */
    initialName?: string;
    initialSurname?: string;
    onSubmit: (data: OnboardingPayload) => void;
    submitting: boolean;
    error: string | null;
}

type SkillLevel = "BEGINNER" | "INTERMEDIATE" | "ADVANCED";
type Sex = "MALE" | "FEMALE" | "OTHER";

interface Step1Fields {
    name: string;
    surname: string;
    username: string;
    age: string;
    sex: Sex | "";
    region: string;
    imageUrl: string;
}

const SKILL_LEVELS: SkillLevel[] = ["BEGINNER", "INTERMEDIATE", "ADVANCED"];
const SKILL_LABEL: Record<SkillLevel, string> = {
    BEGINNER: "Beginner",
    INTERMEDIATE: "Intermediate",
    ADVANCED: "Advanced",
};

const EMPTY_STEP1: Step1Fields = {
    name: "",
    surname: "",
    username: "",
    age: "",
    sex: "",
    region: "",
    imageUrl: "",
};

export function OnboardingForm({
    sports,
    initialName = "",
    initialSurname = "",
    onSubmit,
    submitting,
    error,
}: Props) {
    const [step, setStep] = useState<1 | 2>(1);
    const [fields, setFields] = useState<Step1Fields>({
        ...EMPTY_STEP1,
        name: initialName,
        surname: initialSurname,
    });
    const [selectedSports, setSelectedSports] = useState<Record<number, SkillLevel>>({});
    const [step1Error, setStep1Error] = useState<string | null>(null);

    useEffect(() => {
        const nodes = document.querySelectorAll<HTMLElement>("[data-reveal]");
        nodes.forEach((n) => n.classList.add("is-in"));
    }, [step]);
    // ── Step 1 helpers ───────────────────────────────────────────
    function setField<K extends keyof Step1Fields>(key: K, value: Step1Fields[K]) {
        setFields((prev) => ({ ...prev, [key]: value }));
    }

    function validateStep1(): string | null {
        if (!fields.name.trim())     return "First name is required.";
        if (!fields.surname.trim())  return "Last name is required.";
        if (!fields.username.trim()) return "Username is required.";
        const age = Number(fields.age);
        if (!fields.age || isNaN(age) || age < 10 || age > 100)
            return "Age must be between 10 and 100.";
        if (!fields.sex) return "Please select your sex.";
        return null;
    }

    function handleNext() {
        const err = validateStep1();
        if (err) { setStep1Error(err); return; }
        setStep1Error(null);
        setStep(2);
    }

    // ── Step 2 helpers ───────────────────────────────────────────
    function toggleSport(id: number) {
        setSelectedSports((prev) => {
            if (id in prev) {
                const next = { ...prev };
                delete next[id];
                return next;
            }
            return { ...prev, [id]: "BEGINNER" };
        });
    }

    function setSkill(id: number, level: SkillLevel) {
        setSelectedSports((prev) => ({ ...prev, [id]: level }));
    }

    function handleSubmit() {
        const sportEntries = Object.entries(selectedSports);
        if (sportEntries.length === 0) return;

        const payload: OnboardingPayload = {
            name:     fields.name.trim(),
            surname:  fields.surname.trim(),
            username: fields.username.trim(),
            age:      Number(fields.age),
            sex:      fields.sex as Sex,
            region:   fields.region.trim(),
            imageUrl: fields.imageUrl.trim(),
            sports:   sportEntries.map(([id, skillLevel]): UserSportPayload => ({
                sportId: Number(id),
                skillLevel,
            })),
        };
        onSubmit(payload);
    }

    const sportCount = Object.keys(selectedSports).length;

    return (
        <div className="ob-form">
            {/* ── progress bar ───────────────────────────────────── */}
            <div className="ob-form__progress" aria-hidden="true">
                <div
                    className="ob-form__progress-fill"
                    style={{ "--prog": step === 1 ? "50%" : "100%" } as CSSProperties}
                />
            </div>

            <div className="ob-form__step-label">
                <span className="eyebrow">Step {step} of 2</span>
            </div>

            {/* ══ STEP 1 ══════════════════════════════════════════════ */}
            {step === 1 && (
                <div className="ob-form__body" data-reveal>
                    <h2 className="ob-form__title">Tell us about yourself</h2>
                    <p className="ob-form__lead">
                        This is what other players will see on your profile.
                    </p>

                    <div className="ob-form__grid">
                        <div className="ob-field">
                            <label className="ob-field__label" htmlFor="ob-name">
                                First name <span className="ob-field__req">*</span>
                            </label>
                            <input
                                id="ob-name"
                                className="ob-field__input"
                                type="text"
                                placeholder="e.g. Nikoloz"
                                value={fields.name}
                                onChange={(e) => setField("name", e.target.value)}
                            />
                        </div>

                        <div className="ob-field">
                            <label className="ob-field__label" htmlFor="ob-surname">
                                Last name <span className="ob-field__req">*</span>
                            </label>
                            <input
                                id="ob-surname"
                                className="ob-field__input"
                                type="text"
                                placeholder="e.g. Khuskivadze"
                                value={fields.surname}
                                onChange={(e) => setField("surname", e.target.value)}
                            />
                        </div>

                        <div className="ob-field ob-field--full">
                            <label className="ob-field__label" htmlFor="ob-username">
                                Username <span className="ob-field__req">*</span>
                            </label>
                            <input
                                id="ob-username"
                                className="ob-field__input"
                                type="text"
                                placeholder="e.g. niko_plays"
                                value={fields.username}
                                onChange={(e) => setField("username", e.target.value)}
                            />
                        </div>

                        <div className="ob-field">
                            <label className="ob-field__label" htmlFor="ob-age">
                                Age <span className="ob-field__req">*</span>
                            </label>
                            <input
                                id="ob-age"
                                className="ob-field__input"
                                type="number"
                                placeholder="e.g. 21"
                                min={10}
                                max={100}
                                value={fields.age}
                                onChange={(e) => setField("age", e.target.value)}
                            />
                        </div>

                        <div className="ob-field">
                            <label className="ob-field__label" htmlFor="ob-sex">
                                Sex <span className="ob-field__req">*</span>
                            </label>
                            <select
                                id="ob-sex"
                                className="ob-field__input ob-field__select"
                                value={fields.sex}
                                onChange={(e) => setField("sex", e.target.value as Sex | "")}
                            >
                                <option value="" disabled>Select…</option>
                                <option value="MALE">Male</option>
                                <option value="FEMALE">Female</option>
                                <option value="OTHER">Other</option>
                            </select>
                        </div>

                        <div className="ob-field ob-field--full">
                            <label className="ob-field__label" htmlFor="ob-region">
                                Region
                            </label>
                            <input
                                id="ob-region"
                                className="ob-field__input"
                                type="text"
                                placeholder="e.g. Tbilisi"
                                value={fields.region}
                                onChange={(e) => setField("region", e.target.value)}
                            />
                        </div>

                        <div className="ob-field ob-field--full">
                            <label className="ob-field__label" htmlFor="ob-image">
                                Profile image URL
                            </label>
                            <input
                                id="ob-image"
                                className="ob-field__input"
                                type="url"
                                placeholder="https://…"
                                value={fields.imageUrl}
                                onChange={(e) => setField("imageUrl", e.target.value)}
                            />
                        </div>
                    </div>

                    {step1Error && (
                        <p className="ob-form__error" role="alert">
                            {step1Error}
                        </p>
                    )}

                    <div className="ob-form__actions">
                        <button className="ob-btn ob-btn--solid" onClick={handleNext}>
                            Next — pick your sports →
                        </button>
                    </div>
                </div>
            )}

            {/* ══ STEP 2 ══════════════════════════════════════════════ */}
            {step === 2 && (
                <div className="ob-form__body" data-reveal>
                    <h2 className="ob-form__title">What do you play?</h2>
                    <p className="ob-form__lead">
                        Select every sport you're active in and set your level. You need at
                        least one.
                    </p>

                    <ul className="ob-sports">
                        {sports.map((sport, i) => {
                            const selected = sport.id in selectedSports;
                            const skill = selectedSports[sport.id];
                            return (
                                <li
                                    key={sport.id}
                                    className={`ob-sport ${selected ? "ob-sport--on" : ""}`}
                                    style={{ "--i": i } as CSSProperties}
                                >
                                    <button
                                        className="ob-sport__row"
                                        onClick={() => toggleSport(sport.id)}
                                        aria-pressed={selected}
                                    >
                    <span className="ob-sport__no">
                      {String(i + 1).padStart(2, "0")}
                    </span>
                                        <span className="ob-sport__name">{sport.sportName}</span>
                                        <span className="ob-sport__check">{selected ? "✕" : "+"}</span>
                                    </button>

                                    {selected && (
                                        <div className="ob-sport__levels">
                                            {SKILL_LEVELS.map((lvl) => (
                                                <button
                                                    key={lvl}
                                                    className={`ob-level ${skill === lvl ? "ob-level--on" : ""}`}
                                                    onClick={() => setSkill(sport.id, lvl)}
                                                >
                                                    {SKILL_LABEL[lvl]}
                                                </button>
                                            ))}
                                        </div>
                                    )}
                                </li>
                            );
                        })}
                    </ul>

                    {error && (
                        <p className="ob-form__error" role="alert">
                            {error}
                        </p>
                    )}

                    <div className="ob-form__actions">
                        <button
                            className="ob-btn ob-btn--ghost"
                            onClick={() => setStep(1)}
                            disabled={submitting}
                        >
                            ← Back
                        </button>
                        <button
                            className={`ob-btn ob-btn--solid ${sportCount === 0 ? "ob-btn--disabled" : ""}`}
                            onClick={handleSubmit}
                            disabled={submitting || sportCount === 0}
                        >
                            {submitting
                                ? "Saving…"
                                : `Finish — ${sportCount} sport${sportCount !== 1 ? "s" : ""} selected`}
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
}