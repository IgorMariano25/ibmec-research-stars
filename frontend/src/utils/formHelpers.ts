import type { ApiError } from "../api/types";
import type { UseFormSetError, FieldValues, Path } from "react-hook-form";
import { extractApiError } from "../api/httpClient";

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
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return iso;
  return d.toLocaleDateString("pt-BR");
}

export function formatDateTime(iso?: string | null): string {
  if (!iso) return "—";
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return iso;
  return d.toLocaleString("pt-BR");
}
