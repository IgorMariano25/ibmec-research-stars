import {
  Alert,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Stack,
  TextField,
} from "@mui/material";
import { useEffect, useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import type { Publication, PublicationRequest } from "../../api/types";
import { applyApiFieldErrors } from "../../utils/formHelpers";

const schema = z.object({
  title: z.string().min(2, "Informe o título"),
  link: z.string().url("Informe uma URL válida"),
  publicationDate: z.string().min(1, "Informe a data de publicação"),
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
    defaultValues: { title: "", link: "", publicationDate: "" },
  });

  useEffect(() => {
    if (open) {
      reset({
        title: initial?.title ?? "",
        link: initial?.link ?? "",
        publicationDate: initial?.publicationDate?.slice(0, 10) ?? "",
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
