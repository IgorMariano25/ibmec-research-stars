import type { ApiError } from "../api/types";
import type { UseFormSetError, FieldValues, Path } from "react-hook-form";
import { extractApiError } from "../api/httpClient";

const SAO_PAULO_TIME_ZONE = "America/Sao_Paulo";
const DATE_ONLY_PATTERN = /^(\d{4})-(\d{2})-(\d{2})$/;
const LOCAL_DATE_TIME_PATTERN = /^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2})(?::(\d{2}))?(?:\.\d+)?$/;

export function applyApiFieldErrors<T extends FieldValues>(
  error: unknown,
  setError: UseFormSetError<T>,
): ApiError | null {
  const api = extractApiError(error);
  if (api?.fieldErrors?.length) {
    api.fieldErrors.forEach((fe) => {
      setError(fe.field as Path<T>, { type: "server", message: fe.message });
    });
  }
  return api;
}

export function formatDate(iso?: string | null): string {
  if (!iso) return "—";
  const dateOnly = DATE_ONLY_PATTERN.exec(iso);
  if (dateOnly) {
    const [, year, month, day] = dateOnly;
    return `${day}/${month}/${year}`;
  }
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return iso;
  return d.toLocaleDateString("pt-BR", { timeZone: SAO_PAULO_TIME_ZONE });
}

export function formatDateTime(iso?: string | null): string {
  if (!iso) return "—";
  const localDateTime = LOCAL_DATE_TIME_PATTERN.exec(iso);
  if (localDateTime) {
    const [, year, month, day, hour, minute, second = "00"] = localDateTime;
    return `${day}/${month}/${year}, ${hour}:${minute}:${second}`;
  }
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return iso;
  return d.toLocaleString("pt-BR", { timeZone: SAO_PAULO_TIME_ZONE });
}
