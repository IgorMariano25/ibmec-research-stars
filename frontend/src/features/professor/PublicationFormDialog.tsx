import {
  Alert,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  MenuItem,
  Stack,
  TextField,
} from "@mui/material";
import { useEffect, useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import type { Publication, PublicationRequest, PublicationType } from "../../api/types";
import { applyApiFieldErrors } from "../../utils/formHelpers";

const publicationTypeOptions: { value: PublicationType; label: string }[] = [
  { value: "JOURNAL_ARTICLE", label: "Artigo em periódico" },
  { value: "CONFERENCE_PAPER", label: "Artigo em conferência" },
  { value: "BOOK_CHAPTER", label: "Capítulo de livro" },
  { value: "BOOK", label: "Livro" },
  { value: "EXPANDED_ABSTRACT", label: "Resumo expandido" },
  { value: "SIMPLE_ABSTRACT", label: "Resumo simples" },
  { value: "PROCEEDINGS_WORK", label: "Trabalho em anais" },
  { value: "OTHER", label: "Outro" },
];

const schema = z.object({
  title: z.string().min(2, "Informe o título"),
  link: z.string().url("Informe uma URL válida"),
  publicationDate: z.string().min(1, "Informe a data de publicação"),
  publicationType: z.enum([
    "JOURNAL_ARTICLE",
    "CONFERENCE_PAPER",
    "BOOK_CHAPTER",
    "BOOK",
    "EXPANDED_ABSTRACT",
    "SIMPLE_ABSTRACT",
    "PROCEEDINGS_WORK",
    "OTHER",
  ]),
  abntReference: z.string().min(5, "Informe a referência ABNT"),
});

type FormValues = z.infer<typeof schema>;

interface Props {
  open: boolean;
  initial?: Publication | null;
  warnRevalidation?: boolean;
  loading?: boolean;
  onClose: () => void;
  onSubmit: (values: PublicationRequest) => Promise<void>;
}

export function PublicationFormDialog({
  open,
  initial,
  warnRevalidation,
  loading,
  onClose,
  onSubmit,
}: Props) {
  const [serverError, setServerError] = useState<string | null>(null);
  const {
    register,
    handleSubmit,
    reset,
    setError,
    formState: { errors },
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: {
      title: "",
      link: "",
      publicationDate: "",
      publicationType: "JOURNAL_ARTICLE",
      abntReference: "",
    },
  });

  useEffect(() => {
    if (open) {
      reset({
        title: initial?.title ?? "",
        link: initial?.link ?? "",
        publicationDate: initial?.publicationDate?.slice(0, 10) ?? "",
        publicationType: initial?.publicationType ?? "JOURNAL_ARTICLE",
        abntReference: initial?.abntReference ?? "",
      });
      setServerError(null);
    }
  }, [open, initial, reset]);

  const submit = async (values: FormValues) => {
    setServerError(null);
    try {
      await onSubmit(values);
      onClose();
    } catch (error) {
      const api = applyApiFieldErrors(error, setError);
      setServerError(api?.message ?? "Não foi possível salvar a publicação.");
    }
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>
        {initial ? "Editar publicação" : "Nova publicação"}
      </DialogTitle>
      <form onSubmit={handleSubmit(submit)} noValidate>
        <DialogContent>
          <Stack spacing={2} sx={{ pt: 1 }}>
            {serverError && <Alert severity="error">{serverError}</Alert>}
            {warnRevalidation && initial?.status === "VALIDATED" && (
              <Alert severity="warning">
                Editar uma publicação já validada irá reabri-la para nova
                validação (voltará a PENDENTE).
              </Alert>
            )}
            <TextField
              label="Título"
              fullWidth
              {...register("title")}
              error={Boolean(errors.title)}
              helperText={errors.title?.message}
            />
            <TextField
              label="Link (URL)"
              fullWidth
              placeholder="https://..."
              {...register("link")}
              error={Boolean(errors.link)}
              helperText={
                errors.link?.message ?? "URL pública para a publicação"
              }
            />
            <TextField
              label="Data de publicação"
              type="date"
              fullWidth
              InputLabelProps={{ shrink: true }}
              {...register("publicationDate")}
              error={Boolean(errors.publicationDate)}
              helperText={errors.publicationDate?.message}
            />
            <TextField
              select
              label="Tipo de publicação"
              fullWidth
              {...register("publicationType")}
              error={Boolean(errors.publicationType)}
              helperText={errors.publicationType?.message}
            >
              {publicationTypeOptions.map((option) => (
                <MenuItem key={option.value} value={option.value}>
                  {option.label}
                </MenuItem>
              ))}
            </TextField>
            <TextField
              label="Referência ABNT"
              fullWidth
              multiline
              minRows={3}
              {...register("abntReference")}
              error={Boolean(errors.abntReference)}
              helperText={errors.abntReference?.message}
            />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={onClose} disabled={loading}>
            Cancelar
          </Button>
          <Button type="submit" variant="contained" disabled={loading}>
            {loading ? "Salvando..." : "Salvar"}
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  );
}
