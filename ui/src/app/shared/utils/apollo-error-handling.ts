import { ApolloError } from '@apollo/client';
import { ToastrImplService } from '@shared';
import { ToastrService } from 'ngx-toastr';
import { ERROR_MESSAGE } from '../../core/data/common-labels';

export default function handleApolloError(
    toaster: ToastrService | ToastrImplService,
    error: ApolloError
): never {
    if (error?.cause?.message) {
        toaster?.error(error?.cause?.message);
        throw error;
    }
    toaster?.error(ERROR_MESSAGE);
    throw error;
}
